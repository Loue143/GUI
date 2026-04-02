/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package internal_package;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 *
 * @author Dell
 */
public class Booking_Super_Admin extends javax.swing.JInternalFrame {

    /**
     * Creates new form Booking_Super_Admin
     */
    public Booking_Super_Admin() {
        initComponents();
        loadBookings("");
        
        jTextField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                loadBookings(jTextField1.getText());
            }
        });
    }
    
    private void loadBookings(String searchText) {
        // =====================================================================
        // CRITICAL: CHANGE THIS TO YOUR ACTUAL DATABASE PATH!
        // =====================================================================
        String url = "jdbc:sqlite:BBC.db"; 

        // ---------------------------------------------------------------------
        // THE MERGE: We link tbl_booking (bk) to tbl_seat (s) using bk.S_id = s.S_id
        // We also link the driver directly from the booking table using bk.dr_ID
        // ---------------------------------------------------------------------
        String sql = "SELECT bk.Plate AS PlateNo, " +
                     "dr.F_name AS dr_fname, dr.L_name AS dr_lname, " + 
                     "d.Name AS Destination, " +
                     "u.f_name AS fname, u.l_name AS lname, " +
                     "s.SeatNo AS ActualSeatNo, " + 
                     "bk.Date AS BDate " +
                     "FROM tbl_booking bk " +
                     "LEFT JOIN Tbl_user u ON bk.userid = u.userid " +
                     "LEFT JOIN tbl_destinations d ON bk.De_ID = d.De_ID " +
                     "LEFT JOIN tbl_driver dr ON bk.dr_ID = dr.dr_ID " +  // <-- Linked via Booking's dr_ID
                     "LEFT JOIN tbl_seat s ON bk.S_id = s.S_id ";         // <-- The Front-End Merge!

        boolean isSearch = searchText != null && !searchText.trim().isEmpty();
        if (isSearch) {
            sql += "WHERE bk.Plate LIKE ? OR u.f_name LIKE ? OR u.l_name LIKE ? " +
                   "OR d.Name LIKE ? OR dr.F_name LIKE ? OR dr.L_name LIKE ? OR CAST(s.SeatNo AS TEXT) LIKE ? ";
        }
        
        sql += "ORDER BY bk.B_id DESC";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (isSearch) {
                String queryStr = "%" + searchText.trim() + "%";
                pstmt.setString(1, queryStr); 
                pstmt.setString(2, queryStr); 
                pstmt.setString(3, queryStr); 
                pstmt.setString(4, queryStr); 
                pstmt.setString(5, queryStr); 
                pstmt.setString(6, queryStr); 
                pstmt.setString(7, queryStr); 
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                
                model.setColumnIdentifiers(new String[]{"Plate No.", "Driver", "Destination", "Passenger", "Seat No.", "Date"});
                model.setRowCount(0); 

                while (rs.next()) {
                    String plate = rs.getString("PlateNo") != null ? rs.getString("PlateNo") : "N/A";
                    String dest = rs.getString("Destination") != null ? rs.getString("Destination") : "Unknown Dest";
                    
                    String drFName = rs.getString("dr_fname") != null ? rs.getString("dr_fname") : "";
                    String drLName = rs.getString("dr_lname") != null ? rs.getString("dr_lname") : "";
                    String driver = (drFName + " " + drLName).trim();
                    if (driver.isEmpty()) driver = "Unassigned";

                    String pFName = rs.getString("fname") != null ? rs.getString("fname") : "";
                    String pLName = rs.getString("lname") != null ? rs.getString("lname") : "";
                    String passenger = (pFName + " " + pLName).trim();
                    if (passenger.isEmpty()) passenger = "Unknown Passenger";
                    
                    // Merging the Seat Data safely
                    Object seatObj = rs.getObject("ActualSeatNo");
                    String seatNo = (seatObj != null) ? seatObj.toString() : "N/A"; 
                    
                    String date = rs.getString("BDate");

                    model.addRow(new Object[]{
                        plate,
                        driver,
                        dest,
                        passenger,
                        seatNo, 
                        date
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel7 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jPanel7.setBackground(new java.awt.Color(0, 0, 0));

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Search");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(391, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Plate No.", "Driver", "Destination", "Passenger", "Seat No.", "Date"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
        );

        setBounds(0, 0, 601, 379);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

    }//GEN-LAST:event_jTextField1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
