# Use Node.js as a parent image
FROM node:18-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the package.json and install dependencies
COPY package.json package-lock.json ./
RUN npm install

# Copy the rest of the app
COPY . .

# Build the React app
RUN npm run build

# Expose port 80 for serving the frontend
EXPOSE 3000

# Install serve and start the application
RUN npm install -g serve
CMD ["serve", "-s", "build", "-l", "3000"]

