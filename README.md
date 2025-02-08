# Students Partner

## Overview
Students Partner is an Android application designed to help students access and organize their academic materials efficiently. The app addresses the common problem of unstructured distribution of study materials through WhatsApp and other platforms by providing a centralized, organized platform for accessing academic resources.

## Problem Statement
Currently, students face several challenges:
- Study materials are scattered across WhatsApp groups
- PDFs and notes are not properly organized
- Difficulty in finding specific subject materials
- No structured way to access semester-wise content
- Lack of proper organization by college, subject, and section

## Solution
Students Partner provides:
- Organized hierarchy of academic content
- Easy navigation through college → subject → semester → section structure
- Centralized platform for accessing study materials
- User profile management for personalized experience
- Clean and intuitive user interface

## Technical Architecture

### Authentication
- Firebase Authentication for secure user management
- Email and password-based authentication
- Google Sign-In integration
- User session management

### Data Structure (Firestore)
```
collegeList (collection)
  └── College Name (document)
       └── subjects (collection)
            └── Subject Name (document)
                 └── semesters (collection)
                      └── Semester Number (document)
                           └── sections (collection)
                                └── Section Name (document)
                                     └── materials (collection)
                                          └── Material ID (document)
                                               ├── title
                                               ├── description
                                               ├── fileUrl
                                               ├── uploadedBy
                                               ├── uploadedDate
                                               └── type (PDF/DOC/etc)

users (collection)
  └── User ID (document)
       ├── name
       ├── phone
       ├── email
       ├── profilePicUrl
       ├── college
       ├── subject
       ├── semester
       ├── section
       └── lastUpdated
```

### Key Features

#### User Authentication & Profile
- Login with email/password
- Google Sign-In option
- Registration for new users
- Password reset functionality
- Profile picture upload
- Profile information management
- College and course selection

#### Material Management
- Upload study materials (PDFs, DOCs)
- Organize by subject and semester
- Search functionality
- Download for offline access
- Share materials with others
- Material preview

#### Navigation & UI
- Material Design implementation
- Drawer navigation
- Subject-wise categorization
- Semester-wise organization
- Easy-to-use interface
- Dark mode support

### Technical Stack
- Language: Kotlin
- Platform: Android
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Architecture: MVVM (Model-View-ViewModel)

### Dependencies
```gradle
dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    
    // Google Sign In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    
    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Image Loading
    implementation("de.hdodenhof:circleimageview:3.1.0")
}
```

### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/babbira/studentspartner/
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── Subject.kt
│   │   │   │   │   ├── Material.kt
│   │   │   │   │   └── MaterialDetails.kt
│   │   │   │   └── repository/
│   │   │   │       ├── UserRepository.kt
│   │   │   │       ├── SubjectRepository.kt
│   │   │   │       └── MaterialRepository.kt
│   │   │   ├── ui/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── LoginActivity.kt
│   │   │   │   │   └── RegisterActivity.kt
│   │   │   │   ├── main/
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   └── MainViewModel.kt
│   │   │   │   ├── material/
│   │   │   │   │   ├── ViewMaterialActivity.kt
│   │   │   │   │   ├── ViewMaterialViewModel.kt
│   │   │   │   │   ├── MaterialAdapter.kt
│   │   │   │   │   └── MaterialViewHolder.kt
│   │   │   │   ├── profile/
│   │   │   │   │   ├── ProfileActivity.kt
│   │   │   │   │   └── ProfileViewModel.kt
│   │   │   │   └── common/
│   │   │   │       ├── BaseActivity.kt
│   │   │   │       └── BaseViewModel.kt
│   │   │   ├── utils/
│   │   │   │   ├── Constants.kt
│   │   │   │   ├── Extensions.kt
│   │   │   │   ├── FileUtils.kt
│   │   │   │   └── PDFUtils.kt
│   │   │   └── service/
│   │   │       └── DownloadService.kt
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_main.xml
│   │       │   ├── activity_view_material.xml
│   │       │   ├── item_material.xml
│   │       │   └── ... other layouts ...
│   │       ├── drawable/
│   │       │   ├── ic_pdf.xml
│   │       │   ├── ic_download.xml
│   │       │   └── ... other drawables ...
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   ├── colors.xml
│   │       │   └── themes.xml
│   │       └── menu/
│   │           ├── material_menu.xml
│   │           └── ... other menus ...
│   └── test/
│       ├── java/
│       │   └── com/babbira/studentspartner/
│       │       ├── ViewMaterialViewModelTest.kt
│       │       └── MaterialRepositoryTest.kt
│       └── resources/
└── build.gradle.kts
```

## Features in Detail

### 1. Authentication Flow
- Splash screen checks authentication state
- Login/Register options
- Google Sign-In integration
- Password reset via email
- Session management
- Auto-login for returning users

### 2. Profile Management
- Profile creation wizard
- College selection with search
- Subject selection with add option
- Semester selection (1-10)
- Section selection (A-Z)
- Profile picture upload
- Information update

### 3. Material Management
- Upload study materials
- Organize by subject/semester
- Preview before upload
- Download for offline use
- Share via link
- Delete own uploads
- Report inappropriate content

### 4. User Interface
- Clean Material Design
- Dark mode support
- Responsive layouts
- Loading indicators
- Error handling
- Pull-to-refresh
- Infinite scrolling

### 5. Performance Features
- Material caching
- Image compression
- Lazy loading
- Offline support
- Background sync

## Security Measures
- Firebase Authentication
- Firestore security rules
- File access control
- Data validation
- Session management
- Secure file storage

## Future Enhancements
1. Chat Feature
   - Direct messaging
   - Group discussions
   - File sharing in chats

2. Advanced Search
   - Full-text search
   - Filter by type/date
   - Tags and categories

3. Notifications
   - New material alerts
   - Updates from college
   - Chat notifications

4. Social Features
   - Follow other users
   - Share materials
   - Discussion forums

5. Analytics
   - Usage statistics
   - Popular materials
   - User engagement

## Installation
1. Clone the repository
2. Set up Firebase project
3. Add google-services.json
4. Build and run

## Contributing
1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License
[Add your license details]

## Contact
Developer: Mithun
Email: mithunbabbira@gmail.com
GitHub: https://github.com/mithunbabbira

### Core Functionality Details

#### Profile Management Flow
1. User Registration/Login
2. Profile Creation
   - Name and phone number validation
   - College selection with add option
   - Subject selection with add option
   - Fixed semester selection (1st to 10th)
   - Fixed section selection (A to Z)
3. Profile Update
   - Confirmation dialog before updates
   - Real-time validation
   - Data persistence in Firestore

#### Data Validation Rules
- Name: Required field
- Phone: Required field, numeric only
- College: Required field, selected from dropdown or added new
- Subject: Required field, selected from dropdown or added new
- Semester: Required field, selected from fixed options (1-10)
- Section: Required field, selected from fixed options (A-Z)

### Error Handling
- Network connectivity checks
- Firebase operation error handling
- User input validation
- Proper error messages to users
- Graceful degradation

### UI/UX Features
- Loading indicators during operations
- Disabled states for incomplete forms
- Clear feedback for user actions
- Confirmation dialogs for important actions
- Material Design components usage
- Responsive layout for different screen sizes

### Security Measures
- Firebase Authentication integration
- Data access rules in Firestore
- User data privacy
- Session management
- Secure data transmission

### Performance Considerations
- Efficient Firestore queries
- Data caching
- Lazy loading of dropdowns
- Resource optimization
- Memory management

### Testing
#### Unit Tests
- ViewModel tests
- Repository tests
- Utility function tests

#### Integration Tests
- Firebase integration tests
- UI flow tests
- Data persistence tests

#### Manual Testing Checklist
- Profile creation flow
- Data validation
- Error scenarios
- Network conditions
- Device compatibility

### Development Setup
1. Clone the repository
2. Set up Firebase project
3. Add google-services.json
4. Configure Firestore rules
5. Build and run

### Coding Standards
- Kotlin coding conventions
- MVVM architecture patterns
- Clean code principles
- Documentation requirements
- Code review process

### Version Control
- Git branching strategy
- Commit message conventions
- PR review process
- Release tagging

### CI/CD
- Build automation
- Test automation
- Deployment process
- Version management

### Troubleshooting Guide
Common issues and solutions:
1. Firebase connection issues
2. Data synchronization problems
3. UI rendering issues
4. Performance bottlenecks

### App Screenshots
[Add screenshots of key screens]
- Login/Registration
- Profile Creation
- College Selection
- Subject Selection
- Profile Update

### Release History
- Version 1.0.0
  - Initial release
  - Basic profile management
  - College and subject management
  - Firebase integration

### Known Issues
- List any known bugs or limitations
- Planned fixes
- Workarounds if available

### Support
- Bug reporting process
- Feature request process
- Contact information
- Support channels

### Privacy Policy
- Data collection details
- Data usage information
- User privacy rights
- Data deletion process

### Terms of Service
- Usage terms
- User responsibilities
- Service limitations
- Legal considerations

### Material Viewing Features
- PDF viewing with zoom and scroll support
- Download progress tracking
- Offline access to downloaded materials
- Share functionality
- Material details display (title, description, upload date)
- Material actions (download, share, report)
- Loading state handling
- Error handling with retry options 