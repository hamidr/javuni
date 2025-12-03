package v1

import (
	"sn/internal/server/handlers"
	"sn/internal/server/handlers/v1/chargepoints"

	"github.com/gin-gonic/gin"
)

func RegisterV1APIs(app *gin.Engine, cfg *handlers.Config) {
	handler := chargepoints.NewHandler(cfg.Repo, cfg.Log)

	app.POST("chargepoints", handler.Create)
	app.GET("chargepoints/:id", handler.Retrieve)
	app.GET("chargepoints/nearby", handler.Nearby) //NOTE: Let's talk about this
}
