```mermaid
classDiagram
 class UserInfo {
        -String userId
        -String username
        -String password
        -String email
        -String phoneNumber
        -AccountType accountType
        -AtomicInteger followersCount
        -AtomicInteger followingCount
        +String getUserId()
        +void setUserId(String userId)
        +String getUsername()
        +void setUsername(String username)
        +String getPassword()
        +void setPassword(String password)
        +String getEmail()
        +void setEmail(String email)
        +String getPhoneNumber()
        +void setPhoneNumber(String phoneNumber)
        +AccountType getAccountType()
        +void setAccountType(AccountType accountType)
        +AtomicInteger getFollowersCount()
        +void setFollowersCount(AtomicInteger followersCount)
        +AtomicInteger getFollowingCount()
        +void setFollowingCount(AtomicInteger followingCount)
    }

 class Post {
        -Long id
        -String content
        -String userId
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        
        +Long getId()
        +void setId(Long id)
        +String getContent()
        +void setContent(String content)
        +String getUserId()
        +void setUserId(String userId)
        +LocalDateTime getCreatedAt()
        +void setCreatedAt(LocalDateTime createdAt)
        +LocalDateTime getUpdatedAt()
        +void setUpdatedAt(LocalDateTime updatedAt)
        +void onCreate()
        +void onUpdate()
    }

    class Follower {
        -Long id
        -Long userId
        -Long followerId
        +Long getId()
        +void setId(Long id)
        +Long getUserId()
        +void setUserId(Long userId)
        +Long getFollowerId()
        +void setFollowerId(Long followerId)
        +UserInfo getUser()
        +void setUser(UserInfo user)
    }

    UserInfo "1" -- "0..*" Post : "has"
    UserInfo "1" -- "0..*" Follower : "has"