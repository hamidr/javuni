package handlers

import (
	"sn/internal/lib/interfaces"

	"go.uber.org/zap"
)

type Config struct {
	Log  *zap.SugaredLogger
	Repo interfaces.IChargePointRepo
}
