# RegularReception Frontend

## Overview

فایل‌های استاتیک HTML/CSS/JavaScript برای RegularReception.

## Structure

```
frontend/
├── public/
│   ├── index.html           # صفحه اصلی (Landing Page)
│   ├── dashboard.html       # داشبورد مدیریت
│   └── payment-gateway.html # صفحه پرداخت
├── src/
│   ├── components/          # کامپوننت‌های قابل استفاده مجدد
│   ├── pages/               # صفحات اضافی
│   ├── services/            # سرویس‌های API
│   └── assets/              # تصاویر، آیکون‌ها و...
├── package.json
└── README.md
```

## Development

### شروع سرور محلی:

```bash
cd frontend

# با Python
python -m http.server 3000 --directory public

# یا با npm
npm start
```

دسترسی: http://localhost:3000

### استفاده در Production

فایل‌های استاتیک از طریق Nginx یا مستقیماً از Spring Boot سرو می‌شوند.

## Features

- **Responsive Design** - سازگار با موبایل و تبلت
- **RTL Support** - پشتیبانی کامل از راست به چپ
- **Persian Fonts** - فونت‌های فارسی
- **Bootstrap 5** - Framework UI
- **Chart.js** - نمودارها و گزارشات

## API Integration

Backend API در `http://localhost:8081/api` قرار دارد.

### نمونه فراخوانی API:

```javascript
// Login
fetch('http://localhost:8081/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'admin123'
  })
})
.then(response => response.json())
.then(data => {
  localStorage.setItem('token', data.token);
});

// Get Customers
fetch('http://localhost:8081/api/customers', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('token')
  }
})
.then(response => response.json())
.then(customers => console.log(customers));
```

## Technologies

- HTML5
- CSS3
- JavaScript (ES6+)
- Bootstrap 5
- Chart.js
- Font Awesome

## Future Improvements

- [ ] React/Vue.js migration
- [ ] TypeScript
- [ ] State management (Redux/Vuex)
- [ ] Progressive Web App (PWA)
- [ ] Modern build tools (Vite/Webpack)

## License

MIT
