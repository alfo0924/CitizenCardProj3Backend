

#CitizenCardProj3Backend 架構圖：

```
src/main/java/org/example/_citizencard3/
├── config/
│   ├── SecurityConfig.java          # 安全配置
│   ├── WebConfig.java              # CORS和Web配置
│   └── JwtConfig.java              # JWT配置
│
├── controller/
│   ├── AuthController.java         # 認證控制器
│   ├── UserController.java         # 用戶控制器
│   ├── MovieController.java        # 電影控制器
│   └── StoreController.java        # 商店控制器
│
├── model/
│   ├── User.java                   # 用戶實體
│   ├── Movie.java                  # 電影實體
│   ├── Store.java                  # 商店實體
│   └── enums/
│       └── UserRole.java           # 用戶角色枚舉
│
├── repository/
│   ├── UserRepository.java         # 用戶數據訪問
│   ├── MovieRepository.java        # 電影數據訪問
│   └── StoreRepository.java        # 商店數據訪問
│
├── service/
│   ├── AuthService.java            # 認證服務
│   ├── UserService.java            # 用戶服務
│   ├── MovieService.java           # 電影服務
│   └── StoreService.java           # 商店服務
│
├── security/
│   ├── JwtTokenProvider.java       # JWT令牌提供者
│   └── UserDetailsServiceImpl.java  # 用戶詳情服務實現
│
├── dto/
│   ├── request/                    # 請求DTO
│   │   ├── LoginRequest.java
│   │   └── RegisterRequest.java
│   └── response/                   # 響應DTO
│       ├── LoginResponse.java
│       └── UserResponse.java
│
├── exception/
│   ├── GlobalExceptionHandler.java # 全局異常處理
│   └── CustomException.java        # 自定義異常
│
└── Application.java                # 應用程序入口

src/main/resources/
├── application.properties          # 主配置文件
├── application-dev.properties      # 開發環境配置
└── application-prod.properties     # 生產環境配置
```

這個架構：
1. 遵循了 MVC 模式
2. 實現了基本的 CRUD 功能
3. 包含了安全認證機制
4. 支持前後端分離
5. 易於維護和擴展

主要功能模塊：
1. 用戶認證和授權
2. 電影管理
3. 商店管理
4. 用戶管理

