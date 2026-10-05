# 🚀 TufiDESK PWA (Progressive Web App) Yayınlama Rehberi

Bu klasör (`pwa/`), Android uygulamanızın (TufiDESK) tüm arayüzünü, sekmelerini (Ödevler, Takvim, Tufi AI, İstatistikler, Notlar, Pomodoro Sayacı, Ayarlar ve Windows BSOD hata ekranı), Material 3 yüzen ada menüsünü ve çevrimdışı çalışma motorunu **birebir Progressive Web App (PWA)** olarak barındırır.

---

## 🌐 Nerede Yayınlayabilirsiniz? (En Popüler ve Ücretsiz Seçenekler)

TufiDESK PWA tamamen statik dosyalardan (`index.html`, `styles.css`, `app.js`, `sw.js`, `manifest.json`) oluştuğu için hiçbir sunucu yapılandırması gerektirmez. Aşağıdaki platformların hepsinde **tamamen ücretsiz ve anında SSL (HTTPS)** ile yayınlayabilirsiniz:

---

### Seçenek 1: Firebase Hosting (Şiddetle Önerilen ⭐)
Zaten uygulamanızda Firebase kullandığınız için en hızlı ve entegre yöntemdir.

1. Bilgisayarınızda terminal açın ve Firebase CLI yükleyin:
   ```bash
   npm install -g firebase-tools
   ```
2. Giriş yapın:
   ```bash
   firebase login
   ```
3. Proje dizininde hosting başlatın:
   ```bash
   firebase init hosting
   ```
   - Projenizi seçin (Mevcut Firebase projeniz).
   - Public directory sorulduğunda: `pwa` yazın.
   - Configure as a single-page app (rewrite all urls to /index.html)?: `Yes`
   - Set up automatic builds and deploys with GitHub?: `No` (veya `Yes`)
4. Tek komutla canlıya alın:
   ```bash
   firebase deploy --only hosting
   ```
✅ Tebrikler! Size anında `https://<proje-adiniz>.web.app` adresini verecektir.

---

### Seçenek 2: Vercel (Sürükle - Bırak veya GitHub ile 30 Saniye)
1. [vercel.com](https://vercel.com) adresine gidin ve ücretsiz hesap açın.
2. **Yöntem A (Sürükle - Bırak):** `pwa` klasörünü doğrudan Vercel paneline sürükleyip bırakın.
3. **Yöntem B (GitHub):** GitHub reponuzu Vercel'e bağlayın. Root directory olarak `pwa` klasörünü seçin.
4. **Deploy** butonuna basın.
✅ Anında `https://tufidesk.vercel.app` şeklinde ücretsiz HTTPS alan adı oluşturulur.

---

### Seçenek 3: Netlify (Klasörü Sürükle & Bırak)
1. [netlify.com](https://netlify.com) adresine gidin.
2. Giriş yaptıktan sonra **Sites > Deploy manually** bölümüne gelin.
3. `pwa` klasörünü tarayıcıdaki kutucuğa sürükleyip bırakın.
✅ 5 saniyede siteniz yayında!

---

### Seçenek 4: GitHub Pages
1. GitHub reponuzun ayarlarına (**Settings > Pages**) gidin.
2. Source olarak **Deploy from a branch** seçin.
3. `pwa` klasörünü ana dizine veya `docs` dizinine taşıyarak branch seçin.
✅ `https://kullaniciadi.github.io/reponame` adresinden erişilebilir.

---

## 📱 Kullanıcılar Cihazlarına Nasıl Uygulama Olarak Yükleyecek?

PWA standartlarına (`manifest.json` ve `sw.js`) tam uyumlu olduğu için kullanıcılar web sitesine girdiğinde uygulama gibi cihazlarına kurabilirler:

1. **Android (Chrome / Edge / Samsung Internet):**
   - Sitede Ayarlar sekmesindeki **"TufiDESK'i Cihazına Yükle"** butonuna dokunabilir veya
   - Tarayıcının sağ üstteki 3 noktasına (**⋮**) basıp **"Uygulamayı Yükle"** ya da **"Ana Ekrana Ekle"** diyebilirler.
   - Uygulama telefonun ana ekranına gelir, simgesiyle tam ekran (tarayıcı çubuğu olmadan) bir yerel uygulama gibi çalışır.

2. **iOS (iPhone / iPad - Safari):**
   - Safari'de siteyi açın.
   - Alttaki **Paylaş** (kare ve yukarı ok) simgesine dokunun.
   - **"Ana Ekrana Ekle"** seçeneğini seçin.
   - TufiDESK ikonuyla tıpkı App Store'dan indirilmiş gibi çalışır.

3. **Masaüstü (Windows / Mac / Linux - Chrome & Edge):**
   - Adres çubuğunun sağ tarafındaki **"Yükle"** (monitör / artı) ikonuna basılarak bilgisayara bağımsız bir masaüstü uygulaması olarak kurulur.

---

## 🛠️ PWA Özellikleri
- **Çevrimdışı Çalışma (Offline First):** Service Worker sayesinde internet bağlantısı kopsa dahi tüm ödevler, notlar, takvim ve sayaç açılır.
- **Yerel Veri Saklama:** Tüm ödevler ve notlar cihazın `localStorage` alanında güvenle saklanır.
- **ChatGPT Tarzı Tufi AI Arayüzü:** Akıllı öneri çipleri ve çalışma planlama sohbeti.
- **Pomodoro Odak Sayacı:** Web Audio API ile sentezlenen zil sesi ve titreşim uyarısı.
- **Windows BSOD Easter Egg:** Ayarlar ekranından tetiklenebilen meşhur mavi ekran modu!
