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
public class Admin1_8 extends javax.swing.JInternalFrame {

    /**
     * Creates new form Admin1_6
     */
    public Admin1_8() {
        initComponents();
        loadDestinationData(""); // Load all available destinations initially
        loadBusData("");         // Load all standby buses initially
        setupSearchListeners();  // Activate the search bars
        setupTableClickListeners(); // Activate auto-fill on table click
        
        // Make the Confirm button trigger the database update
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignBusToDestination();
            }
        });
    }
    
    // 1. Establish SQLite Connection
    private Connection getConnection() throws SQLException {
        // Ensure "BBC.db" is in your project root, or use an absolute path
        String url = "jdbc:sqlite:BBC.db"; 
        return DriverManager.getConnection(url);
    }

    // 2. Load Destination Data into Users1 (Top Table)
    private void loadDestinationData() {
        DefaultTableModel model = (DefaultTableModel) Users1.getModel();
        model.setRowCount(0); // Clear existing empty rows

        String sql = "SELECT De_ID, Name, assign_bus, status FROM tbl_destinations WHERE status = 'Available'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("De_ID"));
                String name = rs.getString("Name");
                
                // Handle NULL assign_bus values
                String assignBus = rs.getString("assign_bus");
                if (assignBus == null || assignBus.trim().equalsIgnoreCase("NULL") || assignBus.trim().isEmpty()) {
                    assignBus = "0";
                }
                
                String status = rs.getString("status");

                Object[] row = {id, name, assignBus, status};
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading destination data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 3. Load Bus Data into Users (Bottom Table)
    // 3. Load Bus Data into Users (Bottom Table)
    // 3. Load Bus Data into Users (Bottom Table)
    private void loadBusData() {
        DefaultTableModel model = (DefaultTableModel) Users.getModel();
        model.setRowCount(0); // Clear existing empty rows

        // ADDED: INNER JOIN to get driver details from tbl_driver
        String sql = "SELECT b.Bus_ID, d.F_name, d.L_name, b.Plate_No, b.Bus_type, b.seats "
                   + "FROM tbl_bus b "
                   + "INNER JOIN tbl_driver d ON b.dr_ID = d.dr_ID "
                   + "WHERE b.De_ID IS NULL AND b.dr_ID IS NOT NULL AND b.dr_ID != 'NULL'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Combine the first and last name
                String driverName = rs.getString("F_name") + " " + rs.getString("L_name");
                
                // Add the driverName into the 2nd slot to match your new GUI columns
                Object[] row = {
                    rs.getInt("Bus_ID"),
                    driverName,
                    rs.getString("Plate_No"),
                    rs.getString("Bus_type"),
                    rs.getInt("seats")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading bus data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 2. Load Destination Data into Users1 (Top Table) with Search
    // 2. Load Destination Data into Users1 (Top Table) with Search
    private void loadDestinationData(String searchQuery) {
        DefaultTableModel model = (DefaultTableModel) Users1.getModel();
        model.setRowCount(0); // Clear existing rows

        // Updated LIKE clauses to include assign_bus (treating NULL as '0') and status
        String sql = "SELECT De_ID, Name, assign_bus, status FROM tbl_destinations "
                   + "WHERE status = 'Available' AND ("
                   + "De_ID LIKE ? OR "
                   + "Name LIKE ? OR "
                   + "COALESCE(assign_bus, '0') LIKE ? OR "
                   + "status LIKE ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchParam = "%" + searchQuery + "%";
            pstmt.setString(1, searchParam); // For De_ID
            pstmt.setString(2, searchParam); // For Name
            pstmt.setString(3, searchParam); // For assign_bus
            pstmt.setString(4, searchParam); // For status

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = String.valueOf(rs.getInt("De_ID"));
                    String name = rs.getString("Name");
                    
                    // Handle NULL assign_bus values for the table display
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

    // 3. Load Bus Data into Users (Bottom Table) with Search
    // 3. Load Bus Data into Users (Bottom Table) with Search
    // 3. Load Bus Data into Users (Bottom Table) with Search
    private void loadBusData(String searchQuery) {
        DefaultTableModel model = (DefaultTableModel) Users.getModel();
        model.setRowCount(0); // Clear existing rows

        // ADDED: JOIN and new LIKE clauses for the driver's First and Last name
        String sql = "SELECT b.Bus_ID, d.F_name, d.L_name, b.Plate_No, b.Bus_type, b.seats "
                   + "FROM tbl_bus b "
                   + "INNER JOIN tbl_driver d ON b.dr_ID = d.dr_ID "
                   + "WHERE b.De_ID IS NULL AND b.dr_ID IS NOT NULL AND b.dr_ID != 'NULL' "
                   + "AND (b.Bus_ID LIKE ? OR d.F_name LIKE ? OR d.L_name LIKE ? "
                   + "OR b.Plate_No LIKE ? OR b.Bus_type LIKE ? OR b.seats LIKE ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchParam = "%" + searchQuery + "%";
            pstmt.setString(1, searchParam); // Bus_ID
            pstmt.setString(2, searchParam); // F_name
            pstmt.setString(3, searchParam); // L_name
            pstmt.setString(4, searchParam); // Plate_No
            pstmt.setString(5, searchParam); // Bus_type
            pstmt.setString(6, searchParam); // seats

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
    
    // 4. Setup Key Listeners for Real-Time Search
    private void setupSearchListeners() {
        // Search listener for Destinations (jTextField3)
        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadDestinationData(jTextField3.getText().trim());
            }
        });

        // Search listener for Buses (jTextField4)
        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadBusData(jTextField4.getText().trim());
            }
        });
    }
    
    // Method to assign a bus to a destination and update the counts
    private void assignBusToDestination() {
        // Note: jTextField2 is Destination ID, jTextField1 is Bus ID based on your UI
        String destIdText = jTextField2.getText().trim();
        String busIdText = jTextField1.getText().trim();

        if (destIdText.isEmpty() || busIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Destination ID and Bus ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int destId, busId;
        try {
            destId = Integer.parseInt(destIdText);
            busId = Integer.parseInt(busIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "IDs must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Query 1: Assign the destination ID to the bus
        String updateBusSql = "UPDATE tbl_bus SET De_ID = ? WHERE Bus_ID = ?";
        
        // Query 2: Automatically count how many buses have this De_ID and update assign_bus
        String updateDestSql = "UPDATE tbl_destinations SET assign_bus = (SELECT COUNT(Bus_ID) FROM tbl_bus WHERE De_ID = ?) WHERE De_ID = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction so both queries run together securely

            try (PreparedStatement pstmtBus = conn.prepareStatement(updateBusSql);
                 PreparedStatement pstmtDest = conn.prepareStatement(updateDestSql)) {

                // 1. Execute Bus Update
                pstmtBus.setInt(1, destId);
                pstmtBus.setInt(2, busId);
                int busRowsAffected = pstmtBus.executeUpdate();

                // 2. Execute Destination Quantity Update
                pstmtDest.setInt(1, destId);
                pstmtDest.setInt(2, destId);
                pstmtDest.executeUpdate();

                // 3. Finalize and Refresh
                if (busRowsAffected > 0) {
                    conn.commit(); // Save changes
                    JOptionPane.showMessageDialog(this, "Bus successfully assigned to Destination!");
                    
                    // Clear fields
                    jTextField1.setText("");
                    jTextField2.setText("");
                    
                    // Refresh tables to show new data
                    loadDestinationData("");
                    loadBusData("");
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Assignment failed. Please check if the IDs exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }

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
    
    // 5. Setup Mouse Listeners so clicking a table row fills the text fields
    private void setupTableClickListeners() {
        // Top Table (Destinations -> jTextField2)
        Users1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = Users1.getSelectedRow();
                if (selectedRow != -1) {
                    // Column 0 is Destination ID
                    jTextField2.setText(Users1.getValueAt(selectedRow, 0).toString());
                }
            }
        });

        // Bottom Table (Buses -> jTextField1)
        Users.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = Users.getSelectedRow();
                if (selectedRow != -1) {
                    // Column 0 is Bus ID
                    jTextField1.setText(Users.getValueAt(selectedRow, 0).toString());
                }
            }
        });
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
        jScrollPane2 = new javax.swing.JScrollPane();
        Users1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jTextField4 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
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

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Search");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton1.setText("Confirm");

        jLabel1.setText("Destinatin ID");

        jLabel2.setText("Bus ID");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(48, 48, 48))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(69, 69, 69))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44)
                        .addComponent(jButton1)
                        .addGap(93, 93, 93))))
        );

        setBounds(0, 0, 630, 379);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Users;
    private javax.swing.JTable Users1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
