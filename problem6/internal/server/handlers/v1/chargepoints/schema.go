package chargepoints

import (
	"errors"
	"sn/internal/models"
)

var ErrInvalidStatus = errors.New("invalid state for status")

type ChargePointPostRequest struct {
	Name      string                   `json:"name"`
	Latitude  float64                  `json:"latitude"`
	Longitude float64                  `json:"longitude"`
	Status    models.ChargePointStatus `json:"status"`
}

func (r ChargePointPostRequest) ValidateStatus() error {
	switch r.Status {
	case models.Available, models.Occupied, models.Offline:
		return nil
	default:
		return ErrInvalidStatus
	}
}
