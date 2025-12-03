package main

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	r "sn/internal/repositories"
	"sn/internal/server"
	"sn/internal/server/handlers"
	"sn/pkg/config"
	"sn/pkg/logger"
	"syscall"
	"time"

	"go.uber.org/zap"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

const (
	DB_HOST     = "DB_HOST"
	DB_PORT     = "DB_PORT"
	DB_USER     = "DB_USER"
	DB_PASSWORD = "DB_PASSWORD"
	DB_NAME     = "DB_NAME"
)

func main() {
	log, err := logger.New("rest-api")

	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}

	if err := run(log); err != nil {
		log.Errorw("startup", "ERROR", err)
		log.Sync()
		os.Exit(1)
	}
}

func run(log *zap.SugaredLogger) error {
	log.Infow("starting service", "version", "1")
	defer log.Infow("shutdown complete")

	dsn, err := getDBDSN()
	if err != nil {
		return err
	}

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		return err
	}

	repo := r.NewChargePointsRepo(db)

	cfg := handlers.Config{
		Log:  log,
		Repo: repo,
	}

	r := server.APIMux(&cfg)

	srv := &http.Server{
		Addr:    fmt.Sprintf(":%s", config.GetByKey("API_PORT")),
		Handler: r,
	}

	go func() {
		log.Infow("startup", "status", "api router started", "host", srv.Addr)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("listen: %s\n", err)
		}
	}()

	shutdown := make(chan os.Signal, 1)
	signal.Notify(shutdown, syscall.SIGINT, syscall.SIGTERM)

	<-shutdown
	log.Info("Shutdown Server ...")

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		log.Fatal("Server Shutdown:", err)
	}

	<-ctx.Done()

	log.Info("Server exiting")
	return nil
}

func getDBDSN() (string, error) {
	required := map[string]string{
		DB_HOST:     config.GetByKey(DB_HOST),
		DB_PORT:     config.GetByKey(DB_PORT),
		DB_USER:     config.GetByKey(DB_USER),
		DB_PASSWORD: config.GetByKey(DB_PASSWORD),
		DB_NAME:     config.GetByKey(DB_NAME),
	}

	for key, value := range required {
		if value == "" {
			return "", fmt.Errorf("%s is required", key)
		}
	}

	return fmt.Sprintf(
		"host=%s user=%s password=%s dbname=%s port=%s sslmode=disable TimeZone=UTC",
		required[DB_HOST],
		required[DB_USER],
		required[DB_PASSWORD],
		required[DB_NAME],
		required[DB_PORT],
	), nil
}
