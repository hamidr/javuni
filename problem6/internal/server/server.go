package server

import (
	"sn/internal/server/handlers"
	v1 "sn/internal/server/handlers/v1"
	"time"

	ginzap "github.com/gin-contrib/zap"
	"github.com/gin-gonic/gin"
)

func APIMux(cfg *handlers.Config) *gin.Engine {
	r := gin.New()

	r.Use(ginzap.Ginzap(cfg.Log.Desugar(), time.RFC3339, true))

	r.Use(ginzap.RecoveryWithZap(cfg.Log.Desugar(), true))

	v1.RegisterV1APIs(r, cfg)
	return r
}
