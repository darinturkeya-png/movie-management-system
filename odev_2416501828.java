import java.io.*;
import java.net.*;
import java.util.*;

 public  class odev_2416501828 {

    static String dosyaAdi = "filmler.txt";

     public static  void main(String[] args) throws IOException {
        File file = new File(dosyaAdi);
        if (!file.exists()) {
            file.createNewFile();
            System.out.println("Dosya olusturuldu. API'den veriler cekiliyor...");
            veriCek();
        }
        menu();
    }

    // ===================================================
    // 1. VERİ CEK
    // API: https://api.tvmaze.com/shows?page=0
   
    // ===================================================
     public  static void veriCek() {
        try {
            System.out.println("API'ye baglaniliyor...");

            String urlStr = "https://api.tvmaze.com/shows?page=0";
            URI uri = URI.create(urlStr);
            URL url = uri.toURL();
            HttpURLConnection baglanti = (HttpURLConnection) url.openConnection();
            baglanti.setRequestMethod("GET");

            // JSON okuma - Doktor yontemi
            String veri = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(baglanti.getInputStream()));
            String satir;
            while ((satir = br.readLine()) != null) {
                veri += satir;
            }
            br.close();

            
            FileWriter fw = new FileWriter(dosyaAdi, true);
            int count = 0;

         
            int idx = 0;
            while (count < 30 && idx < veri.length()) {
                int idPos = veri.indexOf("\"id\":", idx);
                if (idPos == -1) break;
                int showStart = veri.lastIndexOf("{", idPos);
                if (showStart == -1) { idx = idPos + 5; continue; }

                int depth = 0;
                int showEnd = showStart;
                for (int i = showStart; i < veri.length(); i++) {
                    if (veri.charAt(i) == '{') depth++;
                    else if (veri.charAt(i) == '}') {
                        depth--;
                        if (depth == 0) { showEnd = i; break; }
                    }
                }

                String show = veri.substring(showStart, showEnd + 1);

                String title = extractStringField(show, "\"name\"");
                if (title.isEmpty() || title.equals("Bilinmiyor")) {
                    idx = showEnd + 1;
                    continue;
                }

                if (!show.contains("\"premiered\"")) {
                    idx = showEnd + 1;
                    continue;
                }

                String year     = extractYear(show);
                String rating   = extractRating(show);
                String runtime  = extractNumberField(show, "\"runtime\"");
                String language = extractStringField(show, "\"language\"");
                String genres   = extractGenres(show);

                if (year.isEmpty())     year     = "0";
                if (rating.isEmpty())   rating   = "0";
                if (runtime.isEmpty())  runtime  = "0";
                if (language.isEmpty()) language = "Bilinmiyor";
                if (genres.isEmpty())   genres   = "Bilinmiyor";

                fw.write(title + ";" + year + ";" + rating + ";" + genres + ";" + runtime + ";" + language + "\n");
                count++;
                idx = showEnd + 1;
            }

            fw.close();
            System.out.println("✓ " + count + " kayit basariyla dosyaya yazildi!");

        } catch (Exception e) {
            System.out.println("HATA: " + e.getMessage());
            kayitHata("veriCek", e.getMessage());
        }
    }

    // ===================================================
    // 2. MENU
    // ===================================================
     public static  void menu() {
        Scanner sc = new Scanner(System.in);
        int secim = -1;

        while (secim != 0) {
            System.out.println("\n========================================");
            System.out.println("   FILM YONETIM SISTEMI");
            System.out.println("========================================");
            System.out.println(" 1. Veri Cek (API)");
            System.out.println(" 2. Listele");
            System.out.println(" 3. Guncelle");
            System.out.println(" 4. Sil");
            System.out.println(" 5. Istatistikler  ");
            System.out.println(" 0. Cikis");
            System.out.println("========================================");
            System.out.print(" Lutfen secim yapiniz: ");

            try {
                secim = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Gecersiz giris!");
                continue;
            }

            switch (secim) {
                case 1: veriCek();       break;
                case 2: listeleMenu(sc); break;
                case 3: guncelle(sc);    break;
                case 4: sil(sc);         break;
                case 5: istatistikler(); break;
                case 0: System.out.println("\nCikiyor... Gule gule!"); break;
                default: System.out.println("Gecersiz secim!");
            }
        }
    }

    // ===================================================
    // 3. LISTELE MENU
    // ===================================================
     public static  void listeleMenu(Scanner sc) {
        System.out.println("\n--- LISTELEME MENUSU ---");
        System.out.println(" 1. Ture gore listele");
        System.out.println(" 2. Yila gore listele");
        System.out.println(" 3. Hepsini listele");
        System.out.print(" Seciminiz: ");

        try {
            int secim = Integer.parseInt(sc.nextLine().trim());
            switch (secim) {
                case 1: tureGoreListele(sc); break;
                case 2: yilaGoreListele(sc); break;
                case 3: hepsiniListele();    break;
                default: System.out.println("Gecersiz secim!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Gecersiz giris!");
        }
    }

     public static  void hepsiniListele() {
        List<String[]> liste = dosyaOku();
        if (liste.isEmpty()) { System.out.println("Kayit bulunamadi!"); return; }

        System.out.println("\n" + String.format("%-4s %-35s %-6s %-5s %-25s %-5s %-10s",
                "No", "Film Adi", "Yil", "Puan", "Tur", "Sure", "Dil"));
        System.out.println("-".repeat(95));

        int i = 1;
        for (String[] f : liste) {
            System.out.println(String.format("%-4d %-35s %-6s %-5s %-25s %-5s %-10s",
                    i++, truncate(f[0], 33), f[1], f[2], truncate(f[3], 23), f[4], f[5]));
        }
        System.out.println("-".repeat(95));
        System.out.println("Toplam: " + liste.size() + " kayit");
    }

     public static  void tureGoreListele(Scanner sc) {
        System.out.println("Hangi tur?");
        System.out.println(" 1. Drama");
        System.out.println(" 2. Action");
        System.out.println(" 3. Comedy");
        System.out.println(" 4. Romance");
        System.out.println(" 5. Horror");
        System.out.println(" 6. Thriller");
        System.out.println(" 7. Crime");
        System.out.println(" 8. Science-Fiction");
        System.out.print(" Seciminiz (veya tur adini yazin): ");
        String girdi = sc.nextLine().trim();

        String ara;
        switch (girdi) {
            case "1": ara = "drama";           break;
            case "2": ara = "action";          break;
            case "3": ara = "comedy";          break;
            case "4": ara = "romance";         break;
            case "5": ara = "horror";          break;
            case "6": ara = "thriller";        break;
            case "7": ara = "crime";           break;
            case "8": ara = "science-fiction"; break;
            default:  ara = girdi.toLowerCase(); break;
        }

        List<String[]> liste = dosyaOku();
        boolean bulundu = false;

        System.out.println("\n" + String.format("%-4s %-35s %-6s %-5s %-25s",
                "No", "Film Adi", "Yil", "Puan", "Tur"));
        System.out.println("-".repeat(78));

        int i = 1;
        for (String[] f : liste) {
            if (f[3].toLowerCase().contains(ara)) {
                System.out.println(String.format("%-4d %-35s %-6s %-5s %-25s",
                        i, truncate(f[0], 33), f[1], f[2], truncate(f[3], 23)));
                bulundu = true;
            }
            i++;
        }
        if (!bulundu) System.out.println("Bu turde kayit bulunamadi.");
    }

     public static  void yilaGoreListele(Scanner sc) {
        System.out.print("Yil girin (ornek: 2015): ");
        String yil = sc.nextLine().trim();
        List<String[]> liste = dosyaOku();
        boolean bulundu = false;

        System.out.println("\n" + String.format("%-4s %-35s %-6s %-5s %-25s",
                "No", "Film Adi", "Yil", "Puan", "Tur"));
        System.out.println("-".repeat(78));

        int i = 1;
        for (String[] f : liste) {
            if (f[1].equals(yil)) {
                System.out.println(String.format("%-4d %-35s %-6s %-5s %-25s",
                        i, truncate(f[0], 33), f[1], f[2], truncate(f[3], 23)));
                bulundu = true;
            }
            i++;
        }
        if (!bulundu) System.out.println("Bu yila ait kayit bulunamadi.");
    }

    // ===================================================
    // 4. GUNCELLE
    // ===================================================
     public static  void guncelle(Scanner sc) {
        System.out.print("\nGuncellenecek film adini girin: ");
        String ara = sc.nextLine().trim().toLowerCase();

        List<String[]> liste = dosyaOku();
        int bulunanIndex = -1;

        for (int i = 0; i < liste.size(); i++) {
            if (liste.get(i)[0].toLowerCase().contains(ara)) {
                String[] f = liste.get(i);
                System.out.println("\nBulunan kayit:");
                System.out.println("  Ad   : " + f[0]);
                System.out.println("  Yil  : " + f[1]);
                System.out.println("  Puan : " + f[2]);
                System.out.println("  Tur  : " + f[3]);
                System.out.print("Guncellemek istediginiz kayit bu mu? (e/h): ");
                String cevap = sc.nextLine().trim().toLowerCase();
                if (cevap.equals("e")) { bulunanIndex = i; break; }
            }
        }

        if (bulunanIndex == -1) {
            System.out.println("Kayit bulunamadi veya iptal edildi.");
            return;
        }

        String[] f = liste.get(bulunanIndex);
        System.out.println("\nHangi alani guncellemek istiyorsunuz?");
        System.out.println(" 1. Film Adi  (mevcut: " + f[0] + ")");
        System.out.println(" 2. Yil       (mevcut: " + f[1] + ")");
        System.out.println(" 3. Puan      (mevcut: " + f[2] + ")");
        System.out.println(" 4. Tur       (mevcut: " + f[3] + ")");
        System.out.print(" Seciminiz: ");

        try {
            int alan = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Yeni deger: ");
            String yeniDeger = sc.nextLine().trim();

            if      (alan == 1) f[0] = yeniDeger;
            else if (alan == 2) f[1] = yeniDeger;
            else if (alan == 3) f[2] = yeniDeger;
            else if (alan == 4) f[3] = yeniDeger;
            else { System.out.println("Gecersiz alan!"); return; }

            dosyaYaz(liste);
            System.out.println("✓ Kayit basariyla guncellendi!");
        } catch (NumberFormatException e) {
            System.out.println("Gecersiz giris!");
        }
    }

    // ===================================================
    // 5. SIL
    // ===================================================
     public static  void sil(Scanner sc) {
        System.out.print("\nSilinecek film adini girin: ");
        String ara = sc.nextLine().trim().toLowerCase();

        List<String[]> liste = dosyaOku();
        int bulunanIndex = -1;

        for (int i = 0; i < liste.size(); i++) {
            if (liste.get(i)[0].toLowerCase().contains(ara)) {
                String[] f = liste.get(i);
                System.out.println("\nBulunan kayit:");
                System.out.println("  Ad   : " + f[0]);
                System.out.println("  Yil  : " + f[1]);
                System.out.println("  Puan : " + f[2]);
                System.out.println("  Tur  : " + f[3]);
                System.out.print("Silmek istediginiz kayit bu mu? (e/h): ");
                String cevap = sc.nextLine().trim().toLowerCase();
                if (cevap.equals("e")) { bulunanIndex = i; break; }
            }
        }

        if (bulunanIndex == -1) {
            System.out.println("Kayit bulunamadi veya iptal edildi.");
            return;
        }

        liste.remove(bulunanIndex);
        dosyaYaz(liste);
        System.out.println("✓ Kayit basariyla silindi!");
    }

    // ===================================================
    // 6. ISTATISTIKLER 
    // ===================================================
     public static  void istatistikler() {
        List<String[]> liste = dosyaOku();
        if (liste.isEmpty()) { System.out.println("Veri yok!"); return; }

        double toplamPuan = 0;
        double enYuksek   = 0;
        double enDusuk    = Double.MAX_VALUE;
        String enIyiFilm  = "";
        String enKotuFilm = "";
        int toplamSure    = 0;

        for (String[] f : liste) {
            try {
                double puan = Double.parseDouble(f[2]);
                toplamPuan += puan;
                if (puan > enYuksek) { enYuksek = puan; enIyiFilm  = f[0]; }
                if (puan > 0 && puan < enDusuk) { enDusuk = puan; enKotuFilm = f[0]; }
            } catch (NumberFormatException e) {}

            try { toplamSure += Integer.parseInt(f[4]); }
            catch (NumberFormatException e) {}
        }

        if (enDusuk == Double.MAX_VALUE) enDusuk = 0;

        System.out.println("\n========== ISTATISTIKLER ==========");
        System.out.println(" Toplam kayit sayisi : " + liste.size());
        System.out.printf(" Ortalama puan       : %.1f%n", toplamPuan / liste.size());
        System.out.println(" En yuksek puan      : " + enYuksek + " - " + enIyiFilm);
        System.out.println(" En dusuk puan       : " + enDusuk  + " - " + enKotuFilm);
        System.out.println(" Toplam sure         : " + toplamSure + " dk (" + toplamSure / 60 + " saat)");
        System.out.println("====================================");
    }

    // ===================================================
    // YARDIMCI METODLAR
    // ===================================================
     public static  List<String[]> dosyaOku() {
        List<String[]> liste = new ArrayList<>();
        try {
            BufferedReader oku = new BufferedReader(new FileReader(dosyaAdi));
            String satir = oku.readLine();
            while (satir != null) {
                if (!satir.trim().isEmpty()) {
                    String[] parcalar = satir.split(";", -1);
                    if (parcalar.length >= 6) {
                        liste.add(parcalar);
                    }
                }
                satir = oku.readLine();
            }
            oku.close();
        } catch (Exception e) {
            System.out.println("Dosya okunamadi: " + e.getMessage());
        }
        return liste;
    }

     public static  void dosyaYaz(List<String[]> liste) {
        try {
            FileWriter fw = new FileWriter(dosyaAdi, false);
            for (String[] f : liste) {
                fw.write(String.join(";", f) + "\n");
            }
            fw.close();
        } catch (Exception e) {
            System.out.println("Dosya yazilamadi: " + e.getMessage());
        }
    }

     public static  String extractStringField(String json, String key) {
        int ki = json.indexOf(key);
        if (ki == -1) return "";
        int colon = json.indexOf(":", ki + key.length());
        if (colon == -1) return "";
        int q1 = json.indexOf("\"", colon + 1);
        if (q1 == -1) return "";
        int q2 = json.indexOf("\"", q1 + 1);
        if (q2 == -1) return "";
        return json.substring(q1 + 1, q2);
    }

     public static  String extractNumberField(String json, String key) {
        int ki = json.indexOf(key);
        if (ki == -1) return "";
        int colon = json.indexOf(":", ki + key.length());
        if (colon == -1) return "";
        int vs = colon + 1;
        while (vs < json.length() && json.charAt(vs) == ' ') vs++;
        if (vs >= json.length() || json.charAt(vs) == 'n') return "";
        int end = vs;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(vs, end).trim();
    }

     public static  String extractYear(String json) {
        int ki = json.indexOf("\"premiered\"");
        if (ki == -1) return "";
        int q1 = json.indexOf("\"", json.indexOf(":", ki) + 1);
        if (q1 == -1 || q1 + 5 > json.length()) return "";
        String val = json.substring(q1 + 1, q1 + 5);
        if (val.matches("\\d{4}")) return val;
        return "";
    }
     public static  String extractRating(String json) {
        int ki = json.indexOf("\"rating\"");
        if (ki == -1) return "0";
        int ki2 = json.indexOf("\"average\"", ki);
        if (ki2 == -1) return "0";
        int colon = json.indexOf(":", ki2);
        if (colon == -1) return "0";
        int vs = colon + 1;
        while (vs < json.length() && json.charAt(vs) == ' ') vs++;
        if (vs >= json.length() || json.charAt(vs) == 'n') return "0";
        int end = vs;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        String val = json.substring(vs, end).trim();
        return val.isEmpty() ? "0" : val;
    }

     public static  String extractGenres(String json) {
        int start = json.indexOf("\"genres\":[");
        if (start == -1) return "";
        int arrStart = json.indexOf("[", start);
        int arrEnd   = json.indexOf("]", arrStart);
        if (arrStart == -1 || arrEnd == -1) return "";
        String arrContent = json.substring(arrStart + 1, arrEnd);
        String result = "";
        int idx = 0;
        while (idx < arrContent.length()) {
            int q1 = arrContent.indexOf("\"", idx);
            if (q1 == -1) break;
            int q2 = arrContent.indexOf("\"", q1 + 1);
            if (q2 == -1) break;
            String genre = arrContent.substring(q1 + 1, q2);
            if (!result.isEmpty()) result += "-";
            result += genre;
            idx = q2 + 1;
        }
        return result;
    }

     public static  String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }

     public static  void kayitHata(String metod, String mesaj) {
        try {
            FileWriter fw = new FileWriter("hata_log.txt", true);
            fw.write(new Date() + " | " + metod + " | " + mesaj + "\n");
            fw.close();
        } catch (Exception e) {}
    }
}