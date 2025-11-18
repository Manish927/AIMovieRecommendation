# Configuration Repository

This repository contains centralized configuration files for all microservices in the Movie Booking System.

## Structure

```
config-repo/
├── application.yml              # Shared configuration for all services
├── application-dev.yml          # Development environment overrides
├── application-prod.yml         # Production environment overrides
├── movie-service.yml            # Movie Service specific configuration
├── payment-service.yml          # Payment Service specific configuration
├── recommendation-service.yml    # Recommendation Service specific configuration
├── user-service.yml             # User Service specific configuration
└── ticket-booking-service.yml   # Ticket Booking Service specific configuration
```

## How It Works

1. **Config Server** (ConfigService) reads configuration files from this repository
2. **Microservices** connect to Config Server and fetch their configuration
3. **Environment-specific** configurations can override default values
4. **Dynamic refresh** allows updating configuration without redeploying services

## Configuration Access

Services can access their configuration via:
- `http://config-service:8888/{application-name}/{profile}`
- Example: `http://config-service:8888/movie-service/dev`

## Environment Variables

Sensitive values should be set via environment variables:
- Database credentials
- API keys
- JWT secrets
- Email credentials

## Updating Configuration

1. Update the appropriate YAML file in this repository
2. Commit changes to Git (if using Git backend)
3. Services will automatically refresh configuration (if refresh enabled)
4. Or use `/actuator/refresh` endpoint to force refresh

## Profiles

- **dev**: Development environment
- **prod**: Production environment
- **default**: Default configuration (no profile)

