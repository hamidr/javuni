package interfaces

import (
	"sn/internal/models"

	"github.com/google/uuid"
)

type IChargePointRepo interface {
	Create(name string, status models.ChargePointStatus, latitude float64, longitude float64) (*models.ChargePoint, error)
	Retrieve(id uuid.UUID) (*models.ChargePoint, error)
	Nearby(Lat float64, Long float64, radius uint64) ([]models.ChargePoint, error)
}
