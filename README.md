# SILA 💪🏼

SILA is a fitness and wellness app designed to help users discover studios, track workouts, and connect with friends. With the help of personalized workout recommendations, SILA makes achieving your health goals easier and more enjoyable. The name **SILA** comes from the Bulgarian and Serbian word *сила*, meaning strength, symbolizing the power of fitness and the strength that users build through their journey.

## About the Project 📌

This group project was developed as part of an Advanced Software Engineering course at TU Wien, following Agile principles, specifically the SCRUM methodology. Our team worked under the guidance of academic advisors to ensure an efficient and scalable solution for fitness studio management.

While every team member contributed to all parts of the project, my role was primarily focused on **UI/UX design**, ensuring that the application is intuitive, user-friendly, and aesthetically pleasing.

## Features 🚀

### Studio & User Management
- Studio owners can register and manage studios.
- Users can explore and like studios.
- Admins can approve or remove studios and manage users.

### Booking & Scheduling
- Users can book classes based on availability.
- Users can discover and book classes based on activity types and locations.

### Reviews & Ratings
- Users can rate and review studios.
- Average ratings displayed for better decisions.

### Friendships & Social Interaction
- Users can connect with friends to share progress and achievements.
- Send invitations to friends for activities or classes they are interested in.
- See if friends have booked a certain activity, making it easier to join them for workouts.

### Recommendation Algorithm
- Personalized activity recommendations using content-based filtering and collaborative filtering.
- Users are clustered based on location (20%) and preferences (80%).
- Uses cosine similarity to compare user clusters and activity features, solving the cold start problem.
- Dynamic studio preferences based on user interactions using a utility matrix (ratings, visits, likes).
- Similar users are found using Euclidean distance, and missing ratings are predicted using a baseline model.

## Technology Stack 🛠️

### Frontend
- Angular for dynamic UI
- Bootstrap for responsive design
- TypeScript, HTML5, CSS3

### Backend
- Spring Boot for RESTful services
- Java for core logic
- Hibernate for ORM
- PostgreSQL for data storage
- Spring Security for authentication
- Maven for dependency management
- JUnit 5 & Mockito for testing

### Algorithm
- Python for implementing the activity matching and recommendation algorithm
  - Utilizes content-based filtering, collaborative filtering, and clustering techniques
  - Uses cosine similarity, Euclidean distance, and baseline models for personalized recommendations
  - For detailed explanations of the algorithm and concepts, check out our [Jupyter notebook](https://github.com/radinavg/SILA/blob/main/datascience/notebook.ipynb)

## Development Process 🔄
Following Agile SCRUM methodology with structured sprints, iterative feedback, and weekly stand-ups.

## Installation Guide 📥

### Running with Docker
1. Clone the repository:
   ```bash
   git clone https://gitlab.com/SILA.git
   ```
2. Navigate to the project directory:
   ```bash
   cd SILA
   ```
3. Start the application using Docker Compose:
   ```bash
   docker-compose up
   ```
   The frontend will be available at [http://localhost:4200](http://localhost:4200), the backend at [http://localhost:8080](http://localhost:8080) and the data science service at [http://localhost:5000](http://localhost:5000).

## Testing & Quality Assurance ✅
- Unit Testing with JUnit 5 and Mockito
- Integration Testing for backend services
- Python tests for the data science service
- Manual testing for UI/UX and overall functionality

## Get Started 🎉
Clone the repository, set up the environment, and enjoy a smarter way to find and manage studio activities!


