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
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;

/**
 *
 * @author Dell
 */
public class Admin1_9 extends javax.swing.JInternalFrame {

    /**
     * Creates new form Admin1_9
     */
    public Admin1_9() {
        initComponents();
        loadDestinationData(""); 
        loadBusData("");         
        setupSearchListeners();  
        setupTableClickListeners();
        
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignBusToDestination();
            }
        });
    }

    // 1. Establish SQLite Connection
    private Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:BBC.db"; 
        return DriverManager.getConnection(url);
    }

    // 2. Load Destination Data into Users1 (Top Table)
    private void loadDestinationData(String searchQuery) {
        DefaultTableModel model = (DefaultTableModel) Users1.getModel();
        model.setRowCount(0); 

        String sql = "SELECT De_ID, Name, assign_bus, status FROM tbl_destinations "
                   + "WHERE status = 'Available' AND ("
                   + "De_ID LIKE ? OR "
                   + "Name LIKE ? OR "
                   + "COALESCE(assign_bus, '0') LIKE ? OR "
                   + "status LIKE ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchParam = "%" + searchQuery + "%";
            pstmt.setString(1, searchParam);
            pstmt.setString(2, searchParam);
            pstmt.setString(3, searchParam);
            pstmt.setString(4, searchParam);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = String.valueOf(rs.getInt("De_ID"));
                    String name = rs.getString("Name");
                    
                    String assignBus = rs.getString("assign_bus");
                    if (assignBus == null || assignBus.trim().equalsIgnoreCase("NULL") || assignBus.trim().isEmpty()) {
                        assignBus = "0";
                    }
                    
                    String status = rs.getString("status");

                    Object[] row = {id, name, assignBus, status};
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading destination data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadBusData(String searchQuery) {
        DefaultTableModel model = (DefaultTableModel) Users.getModel();
        model.setRowCount(0); 

        // Uses De_ID IS NOT NULL to only show buses that already have a destination
        String sql = "SELECT b.Bus_ID, d.F_name, d.L_name, b.Plate_No, b.Bus_type, b.seats "
                   + "FROM tbl_bus b "
                   + "INNER JOIN tbl_driver d ON b.dr_ID = d.dr_ID "
                   + "WHERE b.De_ID IS NOT NULL AND b.dr_ID IS NOT NULL AND b.dr_ID != 'NULL' "
                   + "AND (b.Bus_ID LIKE ? OR d.F_name LIKE ? OR d.L_name LIKE ? "
                   + "OR b.Plate_No LIKE ? OR b.Bus_type LIKE ? OR b.seats LIKE ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchParam = "%" + searchQuery + "%";
            pstmt.setString(1, searchParam); 
            pstmt.setString(2, searchParam); 
            pstmt.setString(3, searchParam); 
            pstmt.setString(4, searchParam); 
            pstmt.setString(5, searchParam); 
            pstmt.setString(6, searchParam); 

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String driverName = rs.getString("F_name") + " " + rs.getString("L_name");
                    
                    Object[] row = {
                        rs.getInt("Bus_ID"),
                        driverName,
                        rs.getString("Plate_No"),
                        rs.getString("Bus_type"),
                        rs.getInt("seats")
                    };
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bus data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupSearchListeners() {
        // Top Table Search (Destinations)
        jTextField7.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadDestinationData(jTextField7.getText().trim());
            }
        });

        // Bottom Table Search (Buses)
        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadBusData(jTextField4.getText().trim());
            }
        });
    }

    // 5. Setup Mouse Listeners so clicking a table row fills the text fields
    private void setupTableClickListeners() {
        // Destinations Table -> jTextField2 (left text box)
        Users1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = Users1.getSelectedRow();
                if (selectedRow != -1) {
                    jTextField2.setText(Users1.getValueAt(selectedRow, 0).toString());
                }
            }
        });

        // Buses Table -> jTextField1 (right text box)
        Users.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = Users.getSelectedRow();
                if (selectedRow != -1) {
                    jTextField1.setText(Users.getValueAt(selectedRow, 0).toString());
                }
            }
        });
    }
    
    // 6. Update logic for Re-Routing (Transaction to update old and new counts)
    private void assignBusToDestination() {
        String destIdText = jTextField2.getText().trim(); // Destination ID text field
        String busIdText = jTextField1.getText().trim();  // Bus ID text field

        if (destIdText.isEmpty() || busIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Destination ID and Bus ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newDestId, busId;
        try {
            newDestId = Integer.parseInt(destIdText);
            busId = Integer.parseInt(busIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "IDs must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String getOldDestSql = "SELECT De_ID FROM tbl_bus WHERE Bus_ID = ?";
        String updateBusSql = "UPDATE tbl_bus SET De_ID = ? WHERE Bus_ID = ?";
        String updateDestCountSql = "UPDATE tbl_destinations SET assign_bus = (SELECT COUNT(Bus_ID) FROM tbl_bus WHERE De_ID = ?) WHERE De_ID = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                int oldDestId = -1;
                
                // Step 1: Find out where the bus is currently assigned
                try (PreparedStatement pstmtGetOld = conn.prepareStatement(getOldDestSql)) {
                    pstmtGetOld.setInt(1, busId);
                    ResultSet rs = pstmtGetOld.executeQuery();
                    if (rs.next()) {
                        oldDestId = rs.getInt("De_ID");
                    }
                }

                if (oldDestId == newDestId) {
                    JOptionPane.showMessageDialog(this, "Bus is already assigned to this destination.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Step 2: Update the Bus to its new Destination
                try (PreparedStatement pstmtBus = conn.prepareStatement(updateBusSql)) {
                    pstmtBus.setInt(1, newDestId);
                    pstmtBus.setInt(2, busId);
                    int busRowsAffected = pstmtBus.executeUpdate();
                    
                    if (busRowsAffected == 0) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Re-route failed. Bus ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Step 3: Recalculate bus count for the OLD destination
                if (oldDestId != -1) {
                    try (PreparedStatement pstmtOldDest = conn.prepareStatement(updateDestCountSql)) {
                        pstmtOldDest.setInt(1, oldDestId);
                        pstmtOldDest.setInt(2, oldDestId);
                        pstmtOldDest.executeUpdate();
                    }
                }

                // Step 4: Recalculate bus count for the NEW destination
                try (PreparedStatement pstmtNewDest = conn.prepareStatement(updateDestCountSql)) {
                    pstmtNewDest.setInt(1, newDestId);
                    pstmtNewDest.setInt(2, newDestId);
                    pstmtNewDest.executeUpdate();
                }

                // Finalize and Refresh
                conn.commit(); 
                JOptionPane.showMessageDialog(this, "Bus successfully re-routed to new destination!");
                
                jTextField1.setText("");
                jTextField2.setText("");
                
                loadDestinationData("");
                loadBusData("");

            } catch (SQLException ex) {
                conn.rollback(); // Cancel transaction on error
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        Users = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jTextField4 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Users1 = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jTextField7 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        Users.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Bus ID", "Driver", "Plate No.", "Bus Type", "Seat Capacity"
            }
        ));
        jScrollPane1.setViewportView(Users);

        jPanel4.setBackground(new java.awt.Color(0, 0, 0));

        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Search");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Users1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Destimation ID", "Name", "Bus QTY.", "Status"
            }
        ));
        jScrollPane2.setViewportView(Users1);

        jPanel7.setBackground(new java.awt.Color(0, 0, 0));

        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Search");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(215, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton1.setText("Update");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jLabel1.setText("Destinatin ID");

        jLabel2.setText("Bus ID");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addGap(75, 75, 75))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(54, 54, 54))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addGap(7, 7, 7)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jButton1))))
        );

        setBounds(0, 0, 630, 379);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Users;
    private javax.swing.JTable Users1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField7;
    // End of variables declaration//GEN-END:variables
}
