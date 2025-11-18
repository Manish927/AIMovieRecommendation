# Multi-stage build for Angular Frontend (Node 18 builder)
FROM node:18-bullseye AS builder

# Set working directory
WORKDIR /app

# Copy package files
COPY frontend/package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Copy source code
COPY frontend/ ./

# Build the Angular application
RUN npm run build -- --configuration production

# Runtime stage with Nginx
FROM rockylinux:9

# Install Nginx and wget for health checks
RUN dnf update -y && \
    dnf install -y nginx wget && \
    dnf clean all && \
    rm -rf /var/cache/dnf

# Create nginx user and directories (guard if already exists)
RUN id -u nginx >/dev/null 2>&1 || useradd -r -s /bin/false nginx && \
    mkdir -p /var/www/html /var/log/nginx /var/cache/nginx /etc/nginx/conf.d && \
    chown -R nginx:nginx /var/www/html /var/log/nginx /var/cache/nginx

# Copy built application from builder stage
COPY --from=builder /app/dist/movie-recommendation/* /var/www/html/

# Copy custom nginx configuration
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]

