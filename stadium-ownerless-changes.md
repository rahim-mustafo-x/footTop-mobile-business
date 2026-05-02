# Stadion uchun "Ownersiz" rejim — Mobile uchun o'zgarishlar

## Qisqacha mohiyat

Endi `SUPER_ADMIN` va `DISTRICT_ADMIN` stadionni **OWNER biriktirmasdan** yaratishi mumkin. Bunday stadionlar tizimda ko'rinadi, lekin ularda **vaqt slotlari generatsiya qilinmaydi** va **onlayn bron qilib bo'lmaydi** — faqat telefon orqali og'zaki bron qilinadi.

Keyinchalik admin shu stadionga OWNER biriktirsa, stadion "to'liq" rejimga o'tadi va slotlar paydo bo'ladi.

---

## 1. Stadion yaratish — `POST /v1/stadiums/create`

**Ruxsat etilgan rollar:** `SUPER_ADMIN`, `DISTRICT_ADMIN`, `OWNER`

### Request body

```json
{
  "name": "string",
  "ownerId": 123,            // ENDI IXTIYORIY (admin uchun)
  "regionId": 1,
  "districtId": 5,
  "description": "string",
  "location": { "lat": 0.0, "lng": 0.0 },
  "type": "FOOTBALL",
  "duration": "MIN_60",
  "capacity": 10,
  "pricePerHour": 100000.0,
  "images": [],
  "isActive": true
}
```

### Qoidalar

| Rol | `ownerId` | Natija |
|---|---|---|
| `OWNER` | e'tiborga olinmaydi | Owner = login bo'lgan user, district = uning districti |
| `SUPER_ADMIN` / `DISTRICT_ADMIN` | **berilsa** | O'sha user owner qilib biriktiriladi |
| `SUPER_ADMIN` / `DISTRICT_ADMIN` | **berilmasa (null)** | Stadion **ownersiz** saqlanadi |

> **Eslatma:** `DISTRICT_ADMIN` faqat o'z districti uchun stadion qo'sha oladi.

### Response

```json
{
  "id": 10,
  "name": "...",
  "ownerId": null,        // ownersiz bo'lsa
  "ownerName": null,      // ownersiz bo'lsa
  "..."
}
```

---

## 2. Stadionni yangilash — `PUT /v1/stadiums/{id}`

**Ruxsat etilgan rollar:** `SUPER_ADMIN`, `DISTRICT_ADMIN`, `OWNER`

### Yangi xulq

- `ownerId` **ixtiyoriy** bo'ldi.
- `ownerId` berilsa — stadionga shu OWNER biriktiriladi (yoki o'zgartiriladi).
- `ownerId` berilmasa (`null`) — mavjud owner **o'zgartirilmaydi** ("tegmaslik" rejimi).

> Ownersiz stadionga keyin OWNER biriktirish uchun shu endpointga `ownerId` yuborib qo'yish kifoya.

---

## 3. Stadion detalini olish — `GET /v1/stadiums/{id}?date=...&duration=...`

### Ownerli stadion (oldingi xulq)

Avvalgidek — owner bo'yicha barcha stadionlari ro'yxati slotlar bilan birga qaytadi.

### Ownersiz stadion (YANGI)

Faqat **bitta stadion** — slotlarsiz qaytadi:

```json
{
  "data": [
    {
      "id": 10,
      "name": "...",
      "ownerId": null,
      "ownerName": null,
      "slots": null,
      "earliestAvailable": null,
      "..."
    }
  ]
}
```

**Mobile UI uchun tavsiya:**
- `ownerId == null` bo'lsa — bron qilish tugmasini yashirish yoki **disabled** qilish.
- O'rniga "Telefon orqali bron qilish" CTA ko'rsatish (stadion telefon raqamiga qo'ng'iroq).
- `slots` bo'limini chiqarmaslik (yoki "Onlayn bron mavjud emas" matni bilan almashtirish).

---

## 4. Stadionlar ro'yxati — `GET /v1/stadiums`

**O'zgarish yo'q** — ownersiz stadionlar ham ro'yxatda avvalgidek ko'rinadi. Filtrlar va paginatsiya bir xil.

Karta ko'rinishida `ownerId == null` ni belgisi sifatida ishlatish mumkin (masalan, "Telefon orqali" badge).

---

## 5. Bron yaratish — `POST /v1/bookings`

### Yangi xato

Agar foydalanuvchi ownersiz stadionga bron yuborsa — quyidagi xato qaytadi:

```json
{
  "status": 400,
  "message": "STADIUM_HAS_NO_OWNER_OFFLINE_BOOKING_ONLY"
}
```

**Mobile uchun:** bu xatoni tutib, foydalanuvchiga "Bu stadionga faqat telefon orqali bron qilish mumkin" deb tushunarli xabar ko'rsatish kerak. Eng yaxshisi — bu ekrangacha yetib bormaslik (3-bo'limdagi UI tavsiyasi orqali).

---

## Migration / mavjud foydalanuvchilarga ta'sir

- Eski stadionlar (allaqachon owner bilan yaratilgan) hech qanday o'zgarmaydi — avvalgidek ishlaydi.
- Yangi xatti-harakat **faqat** ownersiz yaratilgan stadionlarga tegishli.

---

## Tezkor checklist (mobile)

- [ ] Stadion kartasi/detali da `ownerId == null` holatini hisobga olish
- [ ] Slot/bron UI ni `slots == null` bo'lsa yashirish
- [ ] "Telefon orqali bron" CTA qo'shish (stadion `phone` maydonini ishlatib)
- [ ] `STADIUM_HAS_NO_OWNER_OFFLINE_BOOKING_ONLY` xato kodi uchun lokalizatsiya qo'shish
