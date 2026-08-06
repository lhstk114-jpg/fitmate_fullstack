# Build Stage
FROM node:22-alpine AS builder

WORKDIR /app

# 패키지 파일 먼저 복사
COPY package*.json ./

# package-lock.json 기준으로 의존성 설치
RUN npm install

# 소스 코드 복사
COPY . .

# Vite React 운영 빌드
RUN npm run build


# Run Stage
FROM nginx:alpine

# 기본 Nginx 설정 삭제
RUN rm -f /etc/nginx/conf.d/default.conf

# 프로젝트 Nginx 설정 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Vite 빌드 결과물을 Nginx 경로로 복사
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]