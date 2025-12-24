# Swagger/OpenAPI Documentation Setup

## ✅ Configuration Complete

The project now has complete Swagger/OpenAPI documentation configured with Persian (Farsi) support.

## 📋 What Was Configured

### 1. Dependencies (pom.xml)
- **springdoc-openapi-starter-webmvc-ui** version 2.3.0 (already present - newer than requested 2.2.0)
- Dependency is configured and ready to use

### 2. OpenAPI Configuration Class
- **Location**: `src/main/java/com/daryaftmanazam/daryaftcore/config/OpenApiConfig.java`
- **Features**:
  - Title: "دریافت منظم API" (Daryaft Manzam API)
  - Description: "سیستم مدیریت دریافت و اشتراک‌های منظم"
  - Version: 1.0.0
  - Contact Information: Daryaft Development Team
  - License: Apache 2.0
  - Server URLs configured for development and production
  - External documentation links

### 3. Application Configuration (application.yml)
```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
    filter: true
    display-request-duration: true
    doc-expansion: none
  show-actuator: false
```

### 4. Controller Annotations
All controllers have complete OpenAPI annotations:

#### ✅ BusinessController
- @Tag: "Business Management"
- All endpoints documented with @Operation and @ApiResponses

#### ✅ CustomerController
- @Tag: "Customer Management"
- All endpoints documented with @Operation, @ApiResponses, and @Parameter

#### ✅ PaymentController
- @Tag: "Payment Management"
- All endpoints documented with @Operation and @ApiResponses

#### ✅ PaymentPlanController
- @Tag: "Payment Plan Management"
- All endpoints documented with @Operation and @ApiResponses

#### ✅ SubscriptionController
- @Tag: "Subscription Management"
- All endpoints documented with @Operation, @ApiResponses, and @Parameter

#### ✅ HealthCheckController (Updated)
- @Tag: "سلامت سیستم" (System Health) - in Farsi
- @Operation: "بررسی سلامت سیستم" - in Farsi
- Full @ApiResponses documentation

## 🚀 How to Access Swagger UI

### Option 1: Using run.bat (Recommended)
```cmd
run.bat
```

### Option 2: Using Maven directly
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-23
mvnw.cmd spring-boot:run
```

### Option 3: Using PowerShell
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-23"
.\mvnw.cmd spring-boot:run
```

## 📖 Documentation URLs

Once the application is running, access the documentation at:

1. **Swagger UI**: http://localhost:8081/swagger-ui.html
   - Interactive API documentation
   - Test endpoints directly from the browser
   - Persian descriptions for system components

2. **OpenAPI JSON**: http://localhost:8081/api-docs
   - Raw OpenAPI 3.0 specification
   - Can be imported into Postman, Insomnia, etc.

3. **Health Check**: http://localhost:8081/api/health
   - Verify system is running

## 🔧 Features Enabled

- ✅ **Method sorting**: Endpoints sorted by HTTP method
- ✅ **Tag sorting**: API groups sorted alphabetically
- ✅ **Try it out**: Test endpoints directly in Swagger UI
- ✅ **Filtering**: Search/filter capabilities in UI
- ✅ **Request duration**: Display API response times
- ✅ **Persian support**: UI and descriptions support Farsi text
- ✅ **Status codes**: Complete HTTP status code documentation
- ✅ **Request/Response schemas**: Full DTO documentation

## 📝 Persian Language Support

The following components have Persian descriptions:
- API Title and Description
- Server descriptions
- External documentation
- Health Check controller tag and operations
- All error messages and success responses

## 🐛 Troubleshooting

### If Swagger UI doesn't load:
1. Verify the application started successfully
2. Check the port is 8081 (configured in application.yml)
3. Ensure no firewall blocking localhost:8081
4. Check console for any startup errors

### If endpoints are missing:
1. Verify all controllers have @RestController annotation
2. Check @RequestMapping paths are correct
3. Ensure springdoc is scanning the controller package

### For Persian text issues:
1. Ensure UTF-8 encoding is set in application.yml
2. Browser should support UTF-8 rendering
3. Check IDE file encoding settings

## 📊 API Coverage

All major endpoints are documented across:
- Business Management (6 endpoints)
- Customer Management (6 endpoints)
- Payment Management (7 endpoints)
- Payment Plan Management (5 endpoints)
- Subscription Management (5 endpoints)
- System Health (1 endpoint)

**Total: 30+ documented API endpoints**

## 🎯 Next Steps

After starting the application:
1. Open http://localhost:8081/swagger-ui.html
2. Verify all controllers appear in the API list
3. Expand each tag to see available endpoints
4. Test endpoints using "Try it out" button
5. Review request/response schemas
6. Verify Persian text displays correctly

## 📄 Additional Resources

- SpringDoc Documentation: https://springdoc.org/
- OpenAPI Specification: https://swagger.io/specification/
- Application Health: http://localhost:8081/api/health
- H2 Console: http://localhost:8081/h2-console
