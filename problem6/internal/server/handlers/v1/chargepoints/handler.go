package chargepoints

import (
	"net/http"
	"strconv"

	"sn/internal/lib/interfaces"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"go.uber.org/zap"
)

type Handler struct {
	log  *zap.SugaredLogger
	repo interfaces.IChargePointRepo
}

func NewHandler(repo interfaces.IChargePointRepo, log *zap.SugaredLogger) Handler {
	return Handler{log, repo}
}

func (h Handler) Create(c *gin.Context) {
	var req ChargePointPostRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		h.log.Warnw("create: bad request", "err", err)
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if req.ValidateStatus() != nil {
		h.log.Warnw("create: invalid status", "status", req.Status)
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid status"})
		return
	}

	h.log.Infow("create: received request", "name", req.Name)
	cp, err := h.repo.Create(req.Name, req.Status, req.Latitude, req.Longitude)
	if err != nil {
		h.log.Errorw("create: failed to create charge point", "err", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create charge point"})
		return
	}
	c.JSON(http.StatusCreated, cp)
}

func (h Handler) Retrieve(c *gin.Context) {
	id := c.Param("id")
	if id == "" {
		h.log.Warn("retrieve: missing id param")
		c.JSON(http.StatusBadRequest, gin.H{"error": "missing id parameter"})
		return
	}

	uid, err := uuid.Parse(id)
	if err != nil {
		h.log.Warnw("retrieve: invalid uuid", "err", err)
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid id parameter"})
		return
	}

	cp, err := h.repo.Retrieve(uid)
	if err != nil {
		h.log.Errorw("retrieve: failed to retrieve charge point", "err", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to retrieve charge point"})
		return
	}
	h.log.Infow("retrieve: fetching", "id", id)
	c.JSON(http.StatusOK, cp)
}

func (h Handler) Nearby(c *gin.Context) {
	latStr := c.Query("lat")
	lonStr := c.Query("lon")
	radiusStr := c.Query("radius") // in km

	if latStr == "" || lonStr == "" {
		h.log.Warn("nearby: missing lat/lon query params")
		c.JSON(http.StatusBadRequest, gin.H{"error": "lat and lon query parameters are required"})
		return
	}

	lat, err := strconv.ParseFloat(latStr, 64)
	if err != nil {
		h.log.Warnw("nearby: invalid lat", "err", err)
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid lat"})
		return
	}

	lon, err := strconv.ParseFloat(lonStr, 64)
	if err != nil {
		h.log.Warnw("nearby: invalid lon", "err", err)
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid lon"})
		return
	}

	radius, err := strconv.ParseUint(radiusStr, 10, 64)
	if err != nil {
		h.log.Warnw("nearby: invalid radius", "err", err)
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid radius"})
		return
	}

	h.log.Infow("nearby: params", "lat", lat, "lon", lon, "radius", radius)

	cp, err := h.repo.Nearby(lat, lon, radius)
	if err != nil {
		h.log.Errorw("nearby: failed to fetch nearby charge points", "err", err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to fetch nearby charge points"})
		return
	}

	// Return the list of nearby charge points
	c.JSON(http.StatusOK, cp)
}
