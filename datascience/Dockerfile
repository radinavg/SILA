FROM python:3.10-slim

# Install dependencies for psycopg2
RUN apt-get update && \
    apt-get install -y build-essential libpq-dev gcc && \
    apt-get clean

# Set the working directory inside the container
WORKDIR /app

# Copy project files
COPY requirements.txt .
COPY . .

# Install Python dependencies using pip
RUN pip install --no-cache-dir -r requirements.txt

# Expose port 5000 for the application
EXPOSE 5000

# Run the application
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:5000", "wsgi:app"]
