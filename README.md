# Students Partner

## Overview
Students Partner is an Android application designed to help students access and organize their academic materials efficiently. The app provides a centralized platform for managing and accessing academic resources, eliminating the chaos of scattered study materials across various platforms.

## Key Features & Screen Details

### 1. Authentication Screens
#### Login Screen
- **Email/Password login**: Allows users to log in using their email and password.
- **Google Sign-In integration**: Provides an option to log in with Google credentials.
- **"Forgot Password" functionality**: Enables users to reset their password via email.
- **Navigation to registration**: Directs users to the registration screen if they don't have an account.
- **Input validation and error handling**: Ensures all fields are correctly filled and provides feedback.
- **Remember me functionality**: Option to remember user credentials for easier login.

#### Registration Screen
- **Email/Password registration**: Allows new users to create an account.
- **Input validation for all fields**: Ensures data integrity and user feedback.
- **Password strength requirements**: Enforces strong password policies.
- **Terms & Conditions acceptance**: Requires users to accept terms before registration.
- **Error handling with clear messages**: Provides user-friendly error messages.

#### Password Reset Screen
- **Email input for password reset**: Users can request a password reset link.
- **Reset link sending functionality**: Sends a reset link to the user's email.
- **Success/Error notifications**: Informs users of the reset process status.
- **Back to login navigation**: Easy navigation back to the login screen.

### 2. Main Screen (Dashboard)
#### Subject List
- **RecyclerView displaying all subjects**: Lists all subjects in a clean, card-based UI.
- **Add new subject functionality**: Allows users to add new subjects.
- **Subject-wise navigation**: Navigate to materials related to each subject.
- **Pull-to-refresh functionality**: Refreshes the subject list.

#### Navigation Drawer
- **User profile section with image**: Displays user information and profile picture.
- **View Profile option**: Navigate to the profile management screen.
- **View Classmates option**: Access the classmates' details screen.
- **About section**: Provides information about the app.
- **Logout option**: Allows users to log out of the app.
- **Clean Material Design implementation**: Ensures a modern and intuitive UI.

#### Timetable Access
- **Class timetable viewing**: View class schedules.
- **Exam timetable viewing**: Access exam schedules.
- **PDF preview functionality**: Preview timetables in PDF format.
- **Download options**: Download timetables for offline access.

### 3. Profile Management
#### View Profile Screen
- **Display user details**: Shows user information such as name, email, etc.
- **Profile picture with update option**: Users can update their profile picture.
- **Editable fields**: Users can edit:
  - Full Name
  - Phone Number
  - College
  - Combination
  - Semester
  - Section
- **Save changes functionality**: Save updated profile information.
- **Input validation**: Ensures all fields are correctly filled.
- **Real-time updates**: Reflects changes immediately.

### 4. Study Material Management
#### View Materials Screen
- **TabLayout with**:
  - All Materials tab
  - Chapter-wise tab
- **Material cards showing**:
  - Title
  - Description
  - Upload date
  - Uploader name
- **FAB for adding new materials**: Quick access to add new materials.
- **Search functionality**: Search through materials.
- **Sort options**: Sort materials by different criteria.

#### Add New Material Screen
- **Title input**: Enter the title of the material.
- **Description input**: Provide a description.
- **Chapter selection**: Choose the relevant chapter.
- **PDF file selection**: Select a PDF file to upload.
- **Upload progress tracking**: Visual feedback on upload progress.
- **Success/Error handling**: Notifies users of upload status.

### 5. Classmate Details Screen
- **List of classmates**: Displays classmates in a card-based layout.
- **Profile picture display**: Shows profile pictures.
- **Basic information display**: Displays basic details of classmates.
- **Pull-to-refresh**: Refreshes the list of classmates.

### 6. Timetable Management
#### View Timetable Screen
- **PDF preview**: Preview timetables in PDF format.
- **Download option**: Download timetables for offline access.
- **Share functionality**: Share timetables with others.
- **Uploader details**: Shows who uploaded the timetable.
- **Last updated information**: Displays the last update time.

#### Upload Timetable Screen
- **PDF file selection**: Select a PDF file to upload.
- **Upload progress tracking**: Visual feedback on upload progress.
- **Success/Error handling**: Notifies users of upload status.
- **Type selection (Class/Exam)**: Choose the type of timetable.

## Technical Implementation

### Data Structure
```
collegeList/
  ├── {college_name}/
  │   └── combination/
  │       └── {combination_name}/
  │           └── semesters/
  │               └── {semester}/
  │                   ├── subjectList/
  │                   │   └── {subject_name}/
  │                   └── timetables/
  │                       ├── class_timetable
  │                       └── exam_timetable
users/
  └── {user_id}/
      ├── profile_info
      └── uploaded_materials
```

### Security Features
- **Firebase Authentication**: Secure user authentication.
- **Firestore Security Rules**: Protects data access.
- **File access control**: Ensures only authorized access.
- **Data validation**: Validates data before processing.
- **Session management**: Manages user sessions securely.

### Performance Optimizations
- **Image compression**: Reduces image sizes for faster loading.
- **Lazy loading**: Loads data as needed to improve performance.
- **Efficient queries**: Optimizes database queries.
- **Data caching**: Caches data for quicker access.
- **Background operations**: Performs tasks in the background to enhance user experience.

## Installation & Setup
1. Clone repository
2. Configure Firebase
3. Add google-services.json
4. Build and run

## Future Enhancements
1. Chat functionality
2. Advanced search
3. Notifications system
4. Social features
5. Analytics integration

## Contributing
[Standard contribution guidelines]

## License
[License details]

## Contact
Developer: Mithun
Email: mithunbabbira@gmail.com
GitHub: github.com/mithunbabbira 