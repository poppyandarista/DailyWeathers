# 🌤️ Daily Weather

**Daily Weather** adalah aplikasi cuaca modern yang dibuat menggunakan  
**Android Studio**, **Kotlin**, dan **Jetpack Compose**, dengan data cuaca dari **OpenWeather API**.

Aplikasi ini menampilkan informasi cuaca secara real-time berdasarkan lokasi pengguna dengan tampilan UI yang dinamis mengikuti waktu (pagi, siang, malam) serta mendukung mode terang dan gelap.

---

## 🏠 Dashboard Utama

### 📍 Cuaca Berdasarkan Lokasi  
Menampilkan:
- Lokasi saat ini (Kota, Negara)
- Tanggal & jam real-time
- Tombol **Refresh**
- Tombol **Search** untuk mencari lokasi lain

---

## 🌡️ Informasi Cuaca Sekarang

Menampilkan:
- Suhu utama (°C)
- Deskripsi cuaca (contoh: Berawan Tebal)
- Suhu tertinggi & terendah
- Kelembapan
- Kecepatan angin
- Peluang hujan

---

## ⏳ Cuaca Beberapa Jam ke Depan (Hourly Forecast)

- Prediksi cuaca per jam untuk 24 jam ke depan  
- Menampilkan suhu, icon cuaca, kondisi, dan peluang hujan

---

## 📊 Kemungkinan Hujan (Rain Probability)

Grafik atau card khusus yang menampilkan:
- Persentase peluang hujan per jam
- Indikator intensitas (ringan, sedang, berat)

---

## 📝 Informasi Tambahan (Weather Insights)

Card berisi informasi yang diperbarui setiap hari:
- Suhu esok hari terasa seperti berapa
- Kelembapan maksimum hari ini
- Kecepatan angin puncak
- Kondisi cuaca umum hari ini
- Perbandingan cuaca hari ini VS kemarin  
  (misal: “Kelembapan lebih tinggi dari kemarin”)

---

## 📅 Cuaca Beberapa Hari ke Depan (Daily Forecast)

- Prediksi cuaca hingga 7 hari  
- Menampilkan suhu minimum–maksimum, icon cuaca, kondisi, dan kemungkinan hujan

---

## 🌞 Matahari & Bulan

Informasi astronomi:
- Waktu **fajar** & **senja**
- Waktu **matahari terbit** & **terbenam**
- Fase bulan sekarang
- Waktu bulan terbit & terbenam

---

## 🏃 Aktivitas Berdasarkan Cuaca

Rekomendasi apakah aktivitas luar cocok dilakukan hari ini:
- **Lari**
- **Bersepeda**
- **Berkebun**

Aplikasi menilai berdasarkan:
- Suhu
- Angin
- Kelembapan
- Peluang hujan

---

## 🌫️ Kualitas Udara (AQI)

- AQI realtime
- Indikator kesehatan  
  (Sehat / Sedang / Tidak Sehat untuk Sensitif / Berbahaya)

---

## 🎨 Tema & Mode Tampilan

### 🌅 Tema Terang
- **Pagi/Siang** → gradasi biru-ungu lembut  
- **Sore/Malam** → gradasi biru tua

### 🌙 Mode Gelap
- Pagi & siang: tidak terlalu gelap  
- Malam: gelap penuh (deep dark)

---

## 🚩Multi-bahasa
- Mendukung fitur multi-bahasa (Bahasa Indonesia dan Bahasa Inggris)

---

## 📸 Screenshot

### 🌅 Pagi – 🦊 Malam – 🌙 Dark Mode

### 🌅 Pagi 
<div style="display: flex; gap: 10px;">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/pagi1.jpg" width="200">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/pagi2.jpg" width="200">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/pagi3.jpg" width="200">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/pagi4.jpg" width="200">
</div>

### 🦊 Malam 
<div style="display: flex; gap: 10px;">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/malam1.jpg" width="200">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/malam2.jpg" width="200">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/malam3.jpg" width="200">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/malam4.jpg" width="200">
</div>

### 🌙 Dark Mode
<div style="display: flex; gap: 10px; margin-bottom: 10px;">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/dark1.jpg" width="250">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/dark2.jpg" width="250">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/dark3.jpg" width="250">
</div>

<div style="display: flex; gap: 10px;">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/dark4.jpg" width="300">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/dark5.jpg" width="300">
</div>

### 🔍 Search
<div style="display: flex; gap: 10px;">
  <img src="https://raw.githubusercontent.com/poppyandarista/DailyWeathers/master/app/src/main/res/drawable/search.jpg" width="200">
</div>
