# Notification Backend

A comprehensive Spring Boot backend application for managing content schedules and notifications across multiple TV displays. This system provides a RESTful API for content management, user authentication, and TV display coordination.

## Frontend
You can find the frontend application for this backend at [notificationfrontend](https://github.com/HyperDarkmoon/notifmanager).

## 🚀 Features

- **Content Scheduling**: Create, manage, and schedule content across multiple TV displays
- **Multi-TV Support**: Target specific TVs or broadcast to all displays
- **Multiple Content Types**: Support for single/dual/quad images, video content, embedded content, and text
- **User Authentication**: Secure login/signup with role-based access
- **Real-time Scheduling**: Active, upcoming, and immediate content scheduling
- **RESTful API**: Comprehensive REST endpoints for all operations
- **Database Integration**: MySQL database with JPA/Hibernate ORM

## 🏗️ Architecture

### Tech Stack
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Database**: MySQL
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security
- **Build Tool**: Gradle

### Key Components

#### Models
- **ContentSchedule**: Manages scheduled content with timing and TV targeting
- **User**: User authentication and role management
- **TVEnum**: Enumeration of available TV displays (TV1-TV4)

#### Controllers
- **AuthController**: User authentication (signin/signup)
- **ContentScheduleController**: Content management operations
- **TVController**: TV display management
- **DashboardController**: Dashboard data aggregation

#### Services
- **ContentScheduleService**: Business logic for content scheduling
- **UserService**: User management and authentication
- **TVEnumService**: TV display operations

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Gradle 7.0 or higher

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd notificationbackend
```

### 2. Database Setup
Create a MySQL database named `notif`:
```sql
CREATE DATABASE notif;
```

### 3. Configure Database Connection
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/notif
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Run Database Migration (if needed)
If you encounter image URL column length issues, run the provided migration:
```sql
-- Execute the SQL commands in database_migration.sql
```

### 5. Build and Run
```bash
# Using Gradle wrapper (recommended)
./gradlew bootRun

# Or build and run JAR
./gradlew build
java -jar build/libs/notificationbackend-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8090`

## 📡 API Endpoints

### Authentication
```
POST /api/auth/signin      # User login
POST /api/auth/signup      # User registration
```

### Content Management
```
GET    /api/content/all              # Get all content schedules
POST   /api/content                  # Create new content schedule
GET    /api/content/{id}             # Get content by ID
PUT    /api/content/{id}             # Update content schedule
DELETE /api/content/{id}             # Delete content schedule
GET    /api/content/active           # Get currently active schedules
GET    /api/content/upcoming         # Get upcoming schedules
GET    /api/content/immediate        # Get immediate/indefinite schedules
GET    /api/content/tv/{tvName}      # Get schedules for specific TV
GET    /api/content/tv/{tvName}/upcoming  # Get upcoming schedules for TV
```

### TV Management
```
GET /api/tv/all        # Get all available TVs
GET /api/tv/{name}     # Get specific TV information
```

### Dashboard
```
GET /api/dashboard     # Get dashboard data with TV and schedule information
GET /api/dashboard/status  # Get current system status
```

## 📝 Content Types

The system supports multiple content types:

- **IMAGE_SINGLE**: Single image display
- **IMAGE_DUAL**: Two-image layout
- **IMAGE_QUAD**: Four-image grid layout
- **VIDEO**: Video content (MP4, WebM, OGG formats)
- **EMBED**: Embedded content (iframes, videos)
- **TEXT**: Text-based content

## 🎯 TV Targeting

Content can be targeted to specific TVs:
- **TV1**: Display 1
- **TV2**: Display 2
- **TV3**: Display 3
- **TV4**: Display 4

## 📊 Content Scheduling

### Schedule Types
1. **Time-based**: Content with specific start/end times
2. **Immediate**: Content that displays immediately and indefinitely
3. **Active**: Currently displaying content
4. **Upcoming**: Scheduled future content

### Schedule Management
- Create schedules with flexible timing
- Target multiple TVs simultaneously
- Support for up to 4 images per schedule
- Long text content support with LONGTEXT storage

## 🔐 Security

- **Authentication**: Username/password based authentication
- **Role-based Access**: USER and ADMIN roles
- **CORS Configuration**: Cross-origin resource sharing enabled
- **Password Encryption**: BCrypt password encoding
- **Session Management**: Stateless session management

## 🗄️ Database Schema

### Main Tables
- **users**: User authentication and profiles
- **content_schedules**: Content scheduling information
- **content_images**: Image URLs for content (up to 4 per schedule)
- **content_videos**: Video URLs for content
- **content_tv_mapping**: TV targeting relationships

## 🧪 Testing

Run tests using:
```bash
./gradlew test
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/org/hyper/notificationbackend/
│   │   ├── NotificationbackendApplication.java    # Main application class
│   │   ├── config/                                # Configuration classes
│   │   │   ├── PasswordConfig.java
│   │   │   └── WebSecurityConfig.java
│   │   ├── controllers/                           # REST controllers
│   │   │   ├── AuthController.java
│   │   │   ├── ContentScheduleController.java
│   │   │   ├── DashboardController.java
│   │   │   └── TVController.java
│   │   ├── models/                                # Entity models
│   │   │   ├── ContentSchedule.java
│   │   │   ├── TVEnum.java
│   │   │   └── User.java
│   │   ├── repositories/                          # Data repositories
│   │   │   ├── ContentScheduleRepository.java
│   │   │   └── UserRepository.java
│   │   └── services/                              # Business logic
│   │       ├── ContentScheduleService.java
│   │       ├── TVEnumService.java
│   │       └── UserService.java
│   └── resources/
│       └── application.properties                 # Application configuration
└── test/                                          # Test classes
```

## 🔧 Configuration

### Application Properties
```properties
# Server Configuration
server.port=8090

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/notif
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 🚦 Usage Examples

### Creating a Content Schedule
```json
POST /api/content
{
  "title": "Morning Announcement",
  "description": "Daily morning updates",
  "contentType": "IMAGE_SINGLE",
  "imageUrls": ["https://example.com/image.jpg"],
  "startTime": "2025-07-11T08:00:00",
  "endTime": "2025-07-11T09:00:00",
  "targetTVs": ["TV1", "TV2"],
  "active": true
}
```

### Creating Video Content
```json
POST /api/content
{
  "title": "Company Presentation",
  "description": "Quarterly company presentation video",
  "contentType": "VIDEO",
  "videoUrls": ["https://example.com/presentation.mp4"],
  "startTime": "2025-07-11T14:00:00",
  "endTime": "2025-07-11T15:00:00",
  "targetTVs": ["TV1", "TV2", "TV3", "TV4"],
  "active": true
}
```

### Creating Multi-Image Content
```json
POST /api/content
{
  "title": "Product Showcase",
  "description": "Display multiple product images",
  "contentType": "IMAGE_QUAD",
  "imageUrls": [
    "https://example.com/product1.jpg",
    "https://example.com/product2.jpg",
    "https://example.com/product3.jpg",
    "https://example.com/product4.jpg"
  ],
  "targetTVs": ["TV1"],
  "active": true
}
```

### User Authentication
```json
POST /api/auth/signin
{
  "username": "admin",
  "password": "password123"
}
```

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Verify MySQL is running
   - Check database credentials
   - Ensure database `notif` exists

2. **Image URL Storage Issues**
   - Run the database migration script
   - Ensure LONGTEXT column type for image URLs

3. **CORS Issues**
   - CORS is configured to allow all origins
   - Check if specific origins need to be configured


## 📄 License

This project is licensed under the terms specified in the LICENSE file.

## 👥 Authors

- **HyperDarkmoon** - Initial development

