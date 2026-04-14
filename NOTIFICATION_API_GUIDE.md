# Push Notification Integration Guide (Mobile)

## Umumiy arxitektura

```
Mobile App → FCM Token oladi → Backend ga yuboradi → Backend push yuboradi → Mobile App qabul qiladi
```

---

## 1. Firebase sozlash (Mobile)

### Android
1. Firebase Console dan `google-services.json` ni yuklab oling
2. `app/` papkasiga joylashtiring
3. `build.gradle` ga Firebase Messaging dependency qo'shing:
```groovy
implementation 'com.google.firebase:firebase-messaging:24.0.0'
```

### iOS
1. Firebase Console dan `GoogleService-Info.plist` ni yuklab oling
2. Xcode loyihasiga qo'shing
3. APNs sertifikatini Firebase Console ga yuklang

---

## 2. Device Token ro'yxatdan o'tkazish

Ilova birinchi ochilganda yoki user login qilganda FCM tokenini backendga yuborish kerak.

### `POST /v1/notifications/device-token/register`

**Request body:**
```json
{
  "userId": 1,
  "token": "fMx7a9..._FCM_TOKEN_...kQ2",
  "deviceType": "ANDROID"
}
```

| Field | Type | Tavsif |
|-------|------|--------|
| `userId` | Long | Tizimga kirgan foydalanuvchi ID si |
| `token` | String | Firebase dan olingan FCM device token |
| `deviceType` | Enum | `ANDROID`, `IOS` yoki `WEB` |

**Response:**
```json
{
  "success": true,
  "message": "Device token registered",
  "data": null
}
```

**Qachon chaqirish kerak:**
- User login qilganda
- FCM token yangilanganda (`onNewToken` callback)
- Ilova birinchi marta ochilganda (agar user tizimga kirgan bo'lsa)

---

## 3. Device Token o'chirish

User logout qilganda tokenni o'chirish kerak, aks holda eski qurilmaga push kelishda davom etadi.

### `DELETE /v1/notifications/device-token?token=fMx7a9..._FCM_TOKEN_...kQ2`

**Query param:**

| Param | Type | Tavsif |
|-------|------|--------|
| `token` | String | O'chiriladigan FCM token |

**Response:**
```json
{
  "success": true,
  "message": "Device token removed",
  "data": null
}
```

**Qachon chaqirish kerak:**
- User logout qilganda

---

## 4. Mening bildirishnomalarim

### `GET /v1/notifications/my/{userId}`

User o'ziga kelgan barcha notificationlar ro'yxatini olish.

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 15,
      "title": "Bron tasdiqlandi",
      "body": "Mega Arena stadioniga 2026-04-10T15:00 da broningiz tasdiqlandi",
      "type": "BOOKING",
      "targetType": "USER",
      "isRead": false,
      "sentAt": "2026-04-10T14:30:00",
      "readAt": null
    },
    {
      "id": 12,
      "title": "O'yinga qo'shildingiz",
      "body": "Dostlar o'yini o'yiniga muvaffaqiyatli qo'shildingiz",
      "type": "MATCH",
      "targetType": "USER",
      "isRead": true,
      "sentAt": "2026-04-09T18:00:00",
      "readAt": "2026-04-09T18:05:00"
    }
  ]
}
```

**Response fieldlari:**

| Field | Type | Tavsif |
|-------|------|--------|
| `id` | Long | Notification ID (o'qilgan deb belgilash uchun kerak) |
| `title` | String | Notification sarlavhasi |
| `body` | String | Notification matni |
| `type` | Enum | `BOOKING`, `MATCH`, `TOURNAMENT`, `SYSTEM` |
| `targetType` | Enum | `USER` (shaxsiy) yoki `ALL` (umumiy) |
| `isRead` | Boolean | O'qilgan yoki o'qilmaganligini bildiradi |
| `sentAt` | DateTime | Yuborilgan vaqti |
| `readAt` | DateTime | O'qilgan vaqti (null bo'lishi mumkin) |

---

## 5. O'qilmagan bildirishnomalar soni

### `GET /v1/notifications/unread-count/{userId}`

Badge soni uchun ishlatiladi (masalan, qo'ng'iroqcha ustidagi raqam).

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": 3
}
```

---

## 6. Bildirishnomani o'qilgan deb belgilash

### `PUT /v1/notifications/{id}/read`

Bitta notificationni o'qilgan deb belgilash. `id` - notification ro'yxatidan olingan ID.

**Response:**
```json
{
  "success": true,
  "message": "Marked as read",
  "data": null
}
```

**Qachon chaqirish kerak:**
- User notification ustiga bosganda

---

## 7. Barchasini o'qilgan deb belgilash

### `PUT /v1/notifications/read-all/{userId}`

Barcha o'qilmagan notificationlarni bir vaqtda o'qilgan deb belgilash.

**Response:**
```json
{
  "success": true,
  "message": "All marked as read",
  "data": null
}
```

**Qachon chaqirish kerak:**
- "Hammasini o'qilgan deb belgilash" tugmasi bosilganda

---

## 8. Push notification qabul qilish (Mobile)

Backend FCM orqali push yuboradi. Mobile tarafda quyidagi holatlar uchun push notification keladi.

### Push payload formati

Har bir push notification `notification` va `data` bloklaridan iborat:

```json
{
  "notification": {
    "title": "Bron tasdiqlandi",
    "body": "Mega Arena stadioniga 2026-04-10T15:00 da broningiz tasdiqlandi"
  },
  "data": {
    "notificationId": "15",
    "type": "BOOKING",
    "targetType": "USER"
  }
}
```

**`data` fieldlari:**

| Field | Type | Tavsif |
|-------|------|--------|
| `notificationId` | String | Notification ID — `PUT /{id}/read` uchun ishlatiladi |
| `type` | String | `BOOKING`, `MATCH`, `TOURNAMENT`, `SYSTEM` — qaysi ekranga o'tkazish kerakligini aniqlash uchun |
| `targetType` | String | `USER` (shaxsiy) yoki `ALL` (umumiy) |

> **Muhim:** FCM `data` fieldlari doimo `String` tipida keladi. Mobile tarafda parse qilish kerak.

### `type` bo'yicha navigatsiya

| type | Qaysi ekranga o'tkazish kerak |
|------|-------------------------------|
| `BOOKING` | Booking detail ekrani |
| `MATCH` | Match detail ekrani |
| `TOURNAMENT` | Tournament detail ekrani |
| `SYSTEM` | Umumiy notification ekrani |

### Avtomatik pushlar (backend event orqali)

| Holat | Template | Misol matn |
|-------|----------|------------|
| Bron tasdiqlandi | `BOOKING_CONFIRMED` | "Mega Arena stadioniga 2026-04-10T15:00 da broningiz tasdiqlandi" |
| Bron bekor qilindi | `BOOKING_CANCELLED` | "Mega Arena stadioniga broningiz bekor qilindi" |
| O'yinga qo'shildi | `MATCH_JOINED` | "Dostlar o'yini o'yiniga muvaffaqiyatli qo'shildingiz" |
| O'yin to'ldi | `MATCH_FULL` | "Dostlar o'yini o'yinidagi barcha joylar band bo'ldi" |
| O'yinchi chiqdi | `MATCH_PLAYER_LEFT` | "Dostlar o'yini o'yinidan bir o'yinchi chiqdi, joy bo'shadi" |
| Turnirga ro'yxatdan o'tildi | `TOURNAMENT_TEAM_REGISTERED` | "Toshkent Cup turniriga jamoangiz muvaffaqiyatli qo'shildi" |

### Cronli pushlar (eslatmalar)

| Vaqt | Template | Tavsif |
|------|----------|--------|
| Har 30 min | `BOOKING_REMINDER` | Booking boshlanishiga 1 soat qoldi |
| Har 1 soat | `MATCH_REMINDER` | Match boshlanishiga 2 soat qoldi |
| Har kuni 09:00 | `TOURNAMENT_REMINDER` | Ertangi turnirni eslatish |

### Admin/Owner tomonidan yuborilgan pushlar

Admin yoki stadion egasi ixtiyoriy matnda push yuborishi mumkin (aksiya, yangilik va h.k.).

### Android — push qabul qilish namunasi

```kotlin
class MyFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        api.registerDeviceToken(
            DeviceTokenRequest(userId, token, "ANDROID")
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: return

        // data payload dan type va notificationId ni olish
        val data = message.data
        val type = data["type"]                   // "BOOKING", "MATCH", ...
        val notificationId = data["notificationId"] // "15"
        val targetType = data["targetType"]         // "USER", "ALL"

        showNotification(title, body, type, notificationId)
    }

    private fun showNotification(title: String, body: String, type: String?, notificationId: String?) {
        // type ga qarab qaysi ekranga o'tkazishni aniqlash
        val intent = when (type) {
            "BOOKING"    -> Intent(this, BookingDetailActivity::class.java)
            "MATCH"      -> Intent(this, MatchDetailActivity::class.java)
            "TOURNAMENT" -> Intent(this, TournamentDetailActivity::class.java)
            else         -> Intent(this, NotificationListActivity::class.java)
        }
        intent.putExtra("notificationId", notificationId)

        // PendingIntent va NotificationCompat.Builder bilan notification ko'rsatish
        // ...
    }
}
```

---

## 9. Mobile tarafda tavsiya etiladigan flow

```
1. App ochildi
   └── User login qilganmi? → Ha
       ├── FCM token olish
       ├── POST /device-token/register  (token saqlash)
       ├── GET /unread-count/{userId}   (badge yangilash)
       └── GET /my/{userId}             (ro'yxatni yuklash)

2. Push notification keldi (foreground)
   ├── Local notification ko'rsatish
   ├── Badge sonini +1 qilish
   └── Agar notification ekrani ochiq bo'lsa → ro'yxatni qayta yuklash

3. User notification ekraniga kirdi
   ├── GET /my/{userId}
   └── Har bir itemga bosganda → PUT /{id}/read

4. "Hammasini o'qish" bosildi
   ├── PUT /read-all/{userId}
   └── Badge = 0

5. User logout qildi
   ├── DELETE /device-token?token=...
   └── Local notification datani tozalash
```

---

## 10. API endpointlari qisqacha jadvali

| # | Method | Endpoint | Tavsif |
|---|--------|----------|--------|
| 1 | `POST` | `/v1/notifications/device-token/register` | FCM token saqlash |
| 2 | `DELETE` | `/v1/notifications/device-token?token=` | FCM token o'chirish |
| 3 | `GET` | `/v1/notifications/my/{userId}` | Mening notificationlarim |
| 4 | `GET` | `/v1/notifications/unread-count/{userId}` | O'qilmaganlar soni |
| 5 | `PUT` | `/v1/notifications/{id}/read` | Bitta o'qilgan deb belgilash |
| 6 | `PUT` | `/v1/notifications/read-all/{userId}` | Hammasini o'qilgan |

### Faqat Admin/Owner uchun

| # | Method | Endpoint | Tavsif |
|---|--------|----------|--------|
| 7 | `POST` | `/v1/notifications/send` | Bitta userga push yuborish |
| 8 | `POST` | `/v1/notifications/send-to-all` | Barchaga push yuborish |

**Admin push yuborish request body:**
```json
{
  "title": "Aksiya!",
  "body": "Bugun barcha stadionlarda 50% chegirma",
  "type": "SYSTEM",
  "targetType": "ALL",
  "targetUserId": null,
  "senderId": 1
}
```

> Bitta userga yuborishda `targetType: "USER"` va `targetUserId` ni to'ldirish kerak.

---

## 11. Notification type bo'yicha UI tavsiyasi

| Type | Icon | Rang | Ekran |
|------|------|------|-------|
| `BOOKING` | 📅 | Yashil | Booking detail ga o'tish |
| `MATCH` | ⚽ | Ko'k | Match detail ga o'tish |
| `TOURNAMENT` | 🏆 | Sariq | Tournament detail ga o'tish |
| `SYSTEM` | 📢 | Kulrang | Umumiy sahifa |

---

## Savollar bo'lsa backend dasturchiga murojaat qiling.
