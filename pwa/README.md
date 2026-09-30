# TufiDESK Web & iOS PWA

TufiDESK uygulamasının **iOS (iPhone / iPad)**, Android, Mac ve PC üzerinde tarayıcıdan çalışan ve aynı Firestore veritabanına bağlanan PWA (Progressive Web App) sürümüdür.

---

## 🚀 iPhone / iOS Cihazlara Yükleme (App Store'suz Yerel Uygulama Gibi)

1. Bu klasördeki dosyaları (`index.html`, `manifest.json`, `sw.js`) herhangi bir web sunucusuna (Firebase Hosting, GitHub Pages, Netlify veya Vercel) yükleyin.
2. iPhone veya iPad'inizdeki **Safari** tarayıcısından siteyi açın.
3. Safari'nin altındaki **Paylaş (Share)** butonuna basın.
4. **"Ana Ekrana Ekle" (Add to Home Screen)** seçeneğini seçin.
5. Artık TufiDESK, iPhone ana ekranınızda tam ekran çalışan gerçek bir iOS uygulaması gibi açılacaktır!

---

## 🔥 Firebase Hosting ile 1 Komutla Canlıya Alma (Önerilen)

Zaten mevcut olan Firebase `tufidesk` projenizi kullanarak anında yayına alabilirsiniz:

```bash
# pwa klasörünün içindeyken:
firebase login
firebase deploy --only hosting
```

Dağıtım tamamlandığında uygulamanız şu adreste doğrudan canlıya geçer:
👉 **`https://tufidesk.web.app`**

---

## ⚡ Özellikler

- **Netflix Tarzı Profil Seçimi:** Tuna, Elif ve Yönetim profilleri.
- **Canlı Senkronizasyon (Sub-second Realtime):** Android cihazınızda veya iPhone'da bir checkbox'a tıklandığı anda Firestore üzerinden diğer tüm cihazlarda saniyesinde güncellenir.
- **Veriyi Yazan Öncelikli:** Çakışma olmadan her zaman en son yapılan işaretleme geçerli olur.
- **Çevrimdışı Desteği:** Service worker ve Firestore offline cache ile internet kesintilerinde çalışmaya devam eder.
