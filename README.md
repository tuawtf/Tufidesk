<div align="center">

# TufiDESK

Öğrenciler için teslim tarihi, öncelik, takvim, not, odak sayacı ve yapay zeka koçunu tek çatı altında toplayan açık kaynak ders yönetim platformu.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-minSdk%2024%20|%20targetSdk%2036-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore-FFCA28?style=flat-square&logo=firebase&logoColor=black)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/tuawtf/Tufidesk?style=flat-square&color=emerald)](https://github.com/tuawtf/Tufidesk/releases)

<br/>

<a href="https://github.com/tuawtf/Tufidesk/releases">
  <img src="https://img.shields.io/badge/APK'yı%20İndir-GitHub%20Releases-2563EB?style=for-the-badge&logo=android&logoColor=white" alt="GitHub Releases'ten indir" />
</a>

</div>

<br/>

## Screenshots

| | |
|---|---|
| <img src="screenshots/1.png" width="100%" alt="Ana Ekran ve Ödevler" /> | <img src="screenshots/2.png" width="100%" alt="Takvim ve Ders Programı" /> |
| <img src="screenshots/3.png" width="100%" alt="Tufi AI Çalışma Koçu" /> | <img src="screenshots/4.png" width="100%" alt="İstatistikler ve Analiz" /> |
| <img src="screenshots/5.png" width="100%" alt="Notlar ve Klasörler" /> | <img src="screenshots/6.png" width="100%" alt="Odak Sayacı ve Denemeler" /> |

## Features

- [x] **Ödev Takibi:** Teslim tarihleri, aciliyet önceliği, ders bazında filtreleme ve bekleyen/geciken/tamamlanan durum yönetimi.
- [x] **Ders Yönetimi & Programı:** Haftalık etüt ve ders çizelgesi, derse özel renk paletleri ve ikonlar.
- [x] **Takvim:** Aylık ve günlük ajanda görünümü, teslim günlerine özel renkli göstergeler.
- [x] **İstatistikler & Performans:** Tamamlanma oranları, haftalık tempo grafikleri ve çalışma serisi (streak) sayacı.
- [x] **Gelişmiş Notlar:** Özelleştirilebilir klasörler (renk ve ikon seçimi), görsel ekleme, madde işaretli kontrol listeleri, kalın metin ve çizim desteği.
- [x] **Odak Zamanlayıcı (Pomodoro):** Ayarlanabilir çalışma, kısa mola ve uzun mola süreleri; sesli ve titreşimli uyarılar.
- [x] **Deneme Sınavı Takibi:** TYT/AYT ve branş denemeleri için doğru-yanlış girişi, otomatik net hesaplama, hedef net takibi ve sınav analiz notları.
- [x] **Tufi AI Çalışma Koçu:** Ödev odaklı yapay zeka asistanı (günlük görev analizi, geciken ödev tespiti, kişiselleştirilmiş adım adım çalışma planı). ChatGPT tarzı çoklu sohbet geçmişi, Markdown çıktısı ve kullanıcı tanımlı API anahtarı desteği (Google Gemini & DeepSeek).
- [x] **Çoklu Profil Desteği:** Farklı öğrenciler arasında (ör. Tuna / Elif) tek dokunuşla profil değiştirme.
- [x] **Bulut Senkronizasyonu & Yedekleme:** Firebase Firestore ile gerçek zamanlı veri eşitleme, JSON içe/dışa aktarma ve bağlantı teşhis paneli.
- [x] **Kişiselleştirilebilir Arayüz:** Açık/koyu tema, Material You dinamik sistem renkleri ve hazır renk şablonları (Orman, Adaçayı, Alacakaranlık, Fırtına, Açık Sis).
- [x] **Dynamic Island & Yüzen Gezinme:** Üst tarafta bildirim adası, alt tarafta yüzen ada menüsü ve tabletler için optimize edilmiş yatay ekran düzeni.
- [x] **Modüler Araçlar Ekranı:** Modülleri yeniden sıralama, gizleme ve alt gezinme çubuğuna sabitleme.
- [x] **Yerleşik Hata Yakalayıcı:** Windows BSOD (mavi ekran) temalı çökme ekranı, detaylı hata raporlama ve geliştirici ayarları.

## Motivation

Öğrencilik hayatımda ders programı için ayrı, ödevler için ayrı, Pomodoro sayacı ve deneme netleri için apayrı uygulamalar kullanmaktan yorulmuştum. Pazardaki mevcut çözümler ya gereksiz reklamlarla doluydu ya da tek bir amaca odaklanıp bütünsel bir çalışma deneyimi sunamıyordu. 

TufiDESK'i, bir öğrencinin ders masasında ihtiyaç duyabileceği tüm araçları modern, sade ve akıcı bir arayüzde bir araya getirmek amacıyla geliştirdim. Yapay zeka entegrasyonuyla da sadece pasif bir not defteri değil, aktif olarak yönlendiren ve plan çıkaran bir çalışma koçu oluşturmayı hedefledim.

## Tech Used

- **Dil:** [Kotlin 2.2](https://kotlinlang.org/)
- **Kullanıcı Arayüzü:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (BOM 2024.09), [Material Design 3](https://m3.material.io/)
- **Yerel Veritabanı:** [Room 2.7](https://developer.android.com/training/data-storage/room)
- **Bulut & Senkronizasyon:** [Firebase](https://firebase.google.com/) (Cloud Firestore, AI, App Check)
- **Ağ & Serileştirme:** [Retrofit 2](https://square.github.io/retrofit/), [OkHttp 4](https://square.github.io/okhttp/), [Moshi](https://github.com/square/moshi)
- **Asenkron İşlemler:** Kotlin Coroutines & Flow
- **Görsel Yükleme:** [Coil](https://coil-kt.github.io/coil/)
- **Güvenlik & Derleme:** [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin), KSP (Kotlin Symbol Processing), Gradle 9.3.1
- **Mimari:** MVVM (Model-View-ViewModel), Repository Pattern, Clean Architecture prensipleri

## Kurulum / Build

Projeyi yerel ortamınızda derlemek ve çalıştırmak için aşağıdaki adımları izleyin.

### Gereksinimler
- Android Studio (Güncel sürüm)
- JDK 21
- Android SDK (minSdk 24, targetSdk 36)

### Adım Adım Kurulum

1. Repoyu klonlayın:
   ```bash
   git clone https://github.com/tuawtf/Tufidesk.git
   cd Tufidesk
