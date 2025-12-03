package models

import (
	"time"

	"github.com/google/uuid"
)

type ChargePointStatus string

const (
	Available ChargePointStatus = "AVAILABLE"
	Occupied  ChargePointStatus = "OCCUPIED"
	Offline   ChargePointStatus = "OFFLINE"
)

type ChargePoint struct {
	ID        uuid.UUID         `gorm:"type:uuid;default:gen_random_uuid();primaryKey" json:"id"`
	Name      string            `gorm:"type:text;not null" json:"name"`
	Status    ChargePointStatus `gorm:"type:charge_point_status;default:'OFFLINE'" json:"status"`
	Latitude  float64           `gorm:"type:double precision;not null" json:"latitude"`
	Longitude float64           `gorm:"type:double precision;not null" json:"longitude"`
	CreatedAt time.Time         `gorm:"type:timestamptz;default:current_timestamp" json:"created_at"`
	UpdatedAt time.Time         `gorm:"type:timestamptz;default:current_timestamp" json:"updated_at"`
}
