# 🍫 chocominto

**chocominto** adalah aplikasi Android untuk belajar kosakata Bahasa Jepang, menggunakan metode *mnemonic*—yaitu teknik mengingat dengan bantuan cerita atau asosiasi yang mudah diingat. Aplikasi ini dirancang interaktif dan progresif, ideal bagi pengguna yang ingin membangun kosakata secara bertahap.

---

## 📱 Fitur Utama

- 📖 Belajar kosakata berdasarkan **level dari WaniKani**
- 💡 Mnemonik untuk membantu menghafal setiap kata
- 🧠 Mode Quiz untuk menguji kata-kata yang sedang dipelajari
- 🔁 Mode Review untuk mengulang kata-kata yang sudah dihafal
- 💾 Akses offline untuk kata-kata yang sudah dipelajari (tersimpan di SQLite)

---

## 🌿 Cara Penggunaan

### 1. **Pilih Level Kosakata**

- Pengguna memilih level berdasarkan sistem **WaniKani** (misalnya: Level 1-10, 11-20, dst).
- Aplikasi akan menampilkan kata secara **acak dari level yang dipilih**.

### 2. **Pelajari atau Lewati**

Setiap kata yang ditampilkan akan menyertakan **detail lengkap**, meliputi:

- **Cara baca** dalam **hiragana**
- **Arti kata**
- **Reading Mnemonic**: cerita atau asosiasi untuk mengingat cara baca
- **Meaning Mnemonic**: cerita atau asosiasi untuk mengingat arti kata
- **Contoh Kalimat** yang menggunakan kata tersebut

Pengguna kemudian dapat memilih:

- ✅ **Learn This Word**
    
    → Kata akan **ditandai sebagai dipelajari** dan masuk ke daftar quiz.
    
- ⏩ **Skip**
    
    → Kata akan **dilewati dan tidak dimasukkan ke dalam quiz**.
    

### 3. **Mulai Quiz**

- Jika sudah memilih **minimal 5 kata**, pengguna dapat memulai quiz.
- Dalam mode quiz:
    - Detail kata bisa **ditampilkan atau disembunyikan** sesuai kebutuhan.
    - Untuk setiap kata, pengguna dapat memilih:
        - 🔁 **Keep Learning**
            
            → Kata akan **muncul lagi di quiz berikutnya** untuk diulang.
            
        - 🧠 **I've Memorized This Word**
            
            → Kata akan **ditandai sebagai sudah hafal**, dan tidak akan muncul lagi di quiz berikutnya.
            

### 4. **Review Kosakata**

- Kata-kata yang sudah dipelajari akan masuk ke daftar review.
- Pengguna dapat:
    - 🔍 Melihat detail kata secara offline
    - 🎯 Melakukan review ulang seperi mode quiz untuk 10 kata acak dari daftar review

---

## ⚙️ Implementasi Teknis

- **Bahasa**: Java (Android SDK)
- **API**: Data kosakata diambil dari [WaniKani API v2](https://api.wanikani.com/v2/subjects?types=vocabulary)
- **Penyimpanan Lokal**: SQLite untuk menyimpan kata yang dipelajari
- **State Management**: Disimpan menggunakan SharedPreferences dan SQLite
- **Arsitektur**: MVVM sederhana untuk pemisahan logika dan UI
