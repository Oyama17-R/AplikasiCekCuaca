
import java.awt.HeadlessException;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import java.io.*;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author asus
 */
public class AplikasiCuacaGUI extends javax.swing.JFrame {

    /**
     * Creates new form AplikasiCuacaGUI
     */
    public AplikasiCuacaGUI() {
        initComponents();
        btnCek.addActionListener((java.awt.event.ActionEvent evt) -> {
            String kota = kotaField.getText();  // Ambil nama kota dari JTextField
            if (!kota.isEmpty()) {
                cekCuaca(kota);
            } else {
                JOptionPane.showMessageDialog(null, "Silakan masukkan nama kota!");
            }
        });
        btnSimpan.addActionListener((java.awt.event.ActionEvent evt) -> {
            saveWeatherData();  // Panggil fungsi untuk menyimpan data cuaca
        });

        btnLoad.addActionListener((java.awt.event.ActionEvent evt) -> {
            loadWeatherData();  // Panggil fungsi untuk memuat data cuaca
        });

    }

    public boolean isInternetAvailable() {
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
            urlConnect.connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void cekCuaca(String kota) {
        if (!isInternetAvailable()) {
            JOptionPane.showMessageDialog(this, "Tidak ada koneksi internet.");
            return;
        }

        try {
            String apiKey = "3fbf274e18cefa3ccf60b319ea3c57ce";  // Ganti dengan API key dari OpenWeatherMap
            // Menambahkan parameter &lang=id untuk mendapatkan cuaca dalam bahasa Indonesia
            String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + kota + "&appid=" + apiKey + "&units=metric&lang=id";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            StringBuilder response;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject main = jsonResponse.getJSONObject("main");
            JSONArray weather = jsonResponse.getJSONArray("weather");
            JSONObject weatherInfo = weather.getJSONObject(0);
            JSONObject wind = jsonResponse.getJSONObject("wind");

            double temp = main.getDouble("temp");
            String description = weatherInfo.getString("description");
            String icon = weatherInfo.getString("icon");  // Mendapatkan icon cuaca
            double windSpeed = wind.getDouble("speed");

            // Menampilkan informasi cuaca
            try {
                URL iconUrl = new URL("http://openweathermap.org/img/wn/" + icon + "@2x.png");
                ImageIcon imageIcon = new ImageIcon(iconUrl);
                cuacaimage.setIcon(imageIcon);
            } catch (MalformedURLException e) {
                JOptionPane.showMessageDialog(this, "URL gambar cuaca tidak valid.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Gagal mengambil gambar cuaca.");
            }

            // Menampilkan data suhu dan angin dalam bahasa Indonesia
            suhuLabel.setText("Suhu: " + temp + "°C");
            anginLabel.setText("Kecepatan Angin: " + windSpeed + " m/s");
            namaCuaca.setText("Cuaca: " + description);

            // Simpan kota ke dalam daftar kota riwayat
            if (!isCityInComboBox(kota)) {
                CBriwayatkota.addItem(kota);
            }

            // Menampilkan data cuaca ke dalam tabel
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            displayWeatherInTable(kota, date, temp, description);

        } catch (HeadlessException | IOException | JSONException e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mengambil data cuaca.");
        }
    }

    private boolean isCityInComboBox(String city) {
        for (int i = 0; i < CBriwayatkota.getItemCount(); i++) {
            if (CBriwayatkota.getItemAt(i).equalsIgnoreCase(city)) {
                return true;
            }
        }
        return false;
    }

// Menyimpan kota ke dalam file
    private void saveFavoriteCity(String city) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("favorit.txt", true))) {
            writer.write(city);
            writer.newLine();
        } catch (IOException e) {
        }
    }

// Memuat kota favorit dari file
    private void loadFavoriteCities() {
        try (BufferedReader reader = new BufferedReader(new FileReader("favorit.txt"))) {
            String city;
            while ((city = reader.readLine()) != null) {
                if (!isCityInComboBox(city)) {
                    CBriwayatkota.addItem(city);
                }
            }
        } catch (IOException e) {
        }
    }

    private void saveWeatherData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("cuaca_data.csv"))) {
            // Menulis header tabel ke dalam file CSV
            writer.write("Kota,Tanggal,Suhu,Kondisi");
            writer.newLine();

            // Menulis setiap baris data dari JTable ke dalam file CSV
            for (int i = 0; i < cuacaTable.getRowCount(); i++) {
                writer.write(cuacaTable.getValueAt(i, 0) + ",");
                writer.write(cuacaTable.getValueAt(i, 1) + ",");
                writer.write(cuacaTable.getValueAt(i, 2) + ",");
                writer.write(cuacaTable.getValueAt(i, 3) + "");
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this, "Data cuaca berhasil disimpan.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data.");
        }
    }

    private void loadWeatherData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("cuaca_data.csv"))) {
            String line;
            DefaultTableModel model = (DefaultTableModel) cuacaTable.getModel();
            model.setRowCount(0); // Kosongkan tabel sebelum memuat data baru

            // Skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                model.addRow(data);
            }

            JOptionPane.showMessageDialog(this, "Data cuaca berhasil dimuat.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data.");
        }
    }

    private void displayWeatherInTable(String city, String date, double temp, String description) {
        DefaultTableModel model = (DefaultTableModel) cuacaTable.getModel();
        model.addRow(new Object[]{city, date, temp + "°C", description});
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        kotaField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        CBriwayatkota = new javax.swing.JComboBox<>();
        btnCek = new javax.swing.JButton();
        btnSimpan = new javax.swing.JButton();
        cuacaimage = new javax.swing.JLabel();
        btnKeluar = new javax.swing.JButton();
        namaCuaca = new javax.swing.JLabel();
        anginLabel = new javax.swing.JLabel();
        suhuLabel = new javax.swing.JLabel();
        btnLoad = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cuacaTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel2.setText("Kota");

        CBriwayatkota.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));

        btnCek.setText("Cek Cuaca");

        btnSimpan.setText("Simpan");

        btnKeluar.setText("Keluar");
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKeluarActionPerformed(evt);
            }
        });

        btnLoad.setText("Muat Data");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(CBriwayatkota, 0, 97, Short.MAX_VALUE)
                            .addComponent(kotaField))
                        .addGap(18, 18, 18)
                        .addComponent(btnCek, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLoad)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(185, 185, 185)
                                .addComponent(suhuLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cuacaimage, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(namaCuaca, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                    .addComponent(anginLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(320, 320, 320))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(kotaField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(btnCek)
                    .addComponent(btnSimpan)
                    .addComponent(btnKeluar)
                    .addComponent(btnLoad))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(CBriwayatkota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cuacaimage, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(namaCuaca, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(anginLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(suhuLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 36)); // NOI18N
        jLabel1.setText("Cuaca");

        cuacaTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Kota", "Waktu", "Suhu", "Cuaca"
            }
        ));
        jScrollPane1.setViewportView(cuacaTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKeluarActionPerformed
        System.exit(0);        // TODO add your handling code here:
    }//GEN-LAST:event_btnKeluarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AplikasiCuacaGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new AplikasiCuacaGUI().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> CBriwayatkota;
    private javax.swing.JLabel anginLabel;
    private javax.swing.JButton btnCek;
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnLoad;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JTable cuacaTable;
    private javax.swing.JLabel cuacaimage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField kotaField;
    private javax.swing.JLabel namaCuaca;
    private javax.swing.JLabel suhuLabel;
    // End of variables declaration//GEN-END:variables
}
