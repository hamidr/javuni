package repositories

import (
	"sn/internal/models"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type ChargingStationsRepo struct {
	*gorm.DB
}

func NewChargePointsRepo(db *gorm.DB) ChargingStationsRepo {
	return ChargingStationsRepo{db}
}

func (repo ChargingStationsRepo) Create(name string, status models.ChargePointStatus, latitude float64, longitude float64) (*models.ChargePoint, error) {
	var cp models.ChargePoint

	// Use raw SQL to leverage ON CONFLICT DO UPDATE and RETURNING for idempotent insert
	query := repo.DB.Raw(`
			Insert into charge_points (name, status, latitude, longitude)
			Values (?, ?, ?, ?)
			ON CONFLICT (latitude, longitude, name)
			DO UPDATE SET status = EXCLUDED.status,
						updated_at = CURRENT_TIMESTAMP
			RETURNING *`,
		name, status, latitude, longitude).
		First(&cp)

	return &cp, query.Error
}

func (repo ChargingStationsRepo) Retrieve(id uuid.UUID) (*models.ChargePoint, error) {
	var cp models.ChargePoint

	query := repo.DB.First(&cp, "id = ?", id)

	return &cp, query.Error
}

func (repo ChargingStationsRepo) Nearby(lat float64, long float64, radius uint64) ([]models.ChargePoint, error) {
	chargePoints := []models.ChargePoint{}
	radiusInMeters := radius * 1000 // convert km to meters
	query := repo.DB.Raw(`
		SELECT * FROM charge_points
		WHERE ST_DWithin(
			geopoint,
			geography(ST_MakePoint($1, $2)),
			$3
		)
		LIMIT 100`, long, lat, radiusInMeters)

	err := query.Scan(&chargePoints).Error

	return chargePoints, err
}
