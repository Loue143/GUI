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
public class Admin1_4 extends javax.swing.JInternalFrame {

    /**
     * Creates new form Admin1_4
     */
    public Admin1_4() {
        initComponents();
        loadBusData("");           // Load all available buses initially
        loadDriverData("");        // Load all available drivers initially
        setupSearchListeners();    // Activate the search bars
        setupTableClickListeners();// Activate auto-fill when clicking table rows
        
        // Add action listener to the Confirm Button
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignDriverToBus();
            }
        });
    }
    
    private Connection getConnection() throws SQLException {
        // IMPORTANT: Replace "BBC.db" with the actual absolute path to your database
        // if it is not located in the root folder of your NetBeans project.
        // Example: "jdbc:sqlite:C:/Users/Dell/Documents/NetBeansProjects/YourProject/BBC.db"
        String url = "jdbc:sqlite:BBC.db"; 
        return DriverManager.getConnection(url);
    }

    // 2. Load Bus Data into jTable2
    // 2. Load Bus Data into jTable2 (Updated for Search)
    private void loadBusData(String searchQuery) {
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0); // Clear existing rows

        // Added LIKE clauses to filter by ID, Plate No, or Type
        String sql = "SELECT Bus_ID, Plate_No, Bus_type, seats FROM tbl_bus "
                   + "WHERE dr_ID IS NULL AND (Bus_ID LIKE ? OR Plate_No LIKE ? OR Bus_type LIKE ? OR seats LIKE ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            // Add wildcards (%) so it searches anywhere in the text
            String searchParam = "%" + searchQuery + "%";
            pstmt.setString(1, searchParam);
            pstmt.setString(2, searchParam);
            pstmt.setString(3, searchParam);
            pstmt.setString(4, searchParam);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("Bus_ID"),
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

    // 3. Load Driver Data into jTable1 (Updated for Search)
    private void loadDriverData(String searchQuery) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Clear existing rows

        // Added LIKE clauses to filter by ID, First Name, or Last Name
        String sql = "SELECT dr_ID, F_name, L_name, Status FROM tbl_driver "
                   + "WHERE Status = 'Waiting' AND (dr_ID LIKE ? OR F_name LIKE ? OR L_name LIKE ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            String searchParam = "%" + searchQuery + "%";
            pstmt.setString(1, searchParam);
            pstmt.setString(2, searchParam);
            pstmt.setString(3, searchParam);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String driverName = rs.getString("F_name") + " " + rs.getString("L_name");
                    Object[] row = {
                        rs.getInt("dr_ID"),
                        driverName,
                        rs.getString("Status")
                    };
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading driver data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupSearchListeners() {
        // Search listener for Bus (jTextField4)
        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                // Pass whatever is typed into the search method
                loadBusData(jTextField4.getText().trim());
            }
        });

        // Search listener for Driver (jTextField3)
        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                // Pass whatever is typed into the search method
                loadDriverData(jTextField3.getText().trim());
            }
        });
    }
    
    // Method to assign a driver to a bus and update the database
    private void assignDriverToBus() {
        String busIdText = jTextField1.getText().trim();
        String driverIdText = jTextField2.getText().trim();

        // 1. Validate that the fields are not empty
        if (busIdText.isEmpty() || driverIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Bus ID and Driver ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int busId, driverId;
        try {
            busId = Integer.parseInt(busIdText);
            driverId = Integer.parseInt(driverIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "IDs must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // SQL queries for the updates
        String updateBusSql = "UPDATE tbl_bus SET dr_ID = ? WHERE Bus_ID = ?";
        String updateDriverSql = "UPDATE tbl_driver SET Status = 'Assigned' WHERE dr_ID = ?";

        try (Connection conn = getConnection()) {
            // 2. Disable auto-commit to use a transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtBus = conn.prepareStatement(updateBusSql);
                 PreparedStatement pstmtDriver = conn.prepareStatement(updateDriverSql)) {

                // 3. Execute Bus update
                pstmtBus.setInt(1, driverId);
                pstmtBus.setInt(2, busId);
                int busRowsAffected = pstmtBus.executeUpdate();

                // 4. Execute Driver update
                pstmtDriver.setInt(1, driverId);
                int driverRowsAffected = pstmtDriver.executeUpdate();

                // 5. Check if both updates were successful
                if (busRowsAffected > 0 && driverRowsAffected > 0) {
                    conn.commit(); // Save the changes to the database
                    JOptionPane.showMessageDialog(this, "Driver successfully assigned to the Bus!");
                    
                    // Clear the input fields
                    jTextField1.setText("");
                    jTextField2.setText("");
                    
                    // Refresh the tables to hide the assigned driver and bus
                    loadBusData("");
                    loadDriverData("");
                } else {
                    conn.rollback(); // Cancel the transaction if something went wrong
                    JOptionPane.showMessageDialog(this, "Assignment failed. Please check if the IDs exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                conn.rollback(); // Cancel if there's an SQL error
                throw ex;
            } finally {
                conn.setAutoCommit(true); // Reset auto-commit
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void setupTableClickListeners() {
        // When a user clicks a row in the Bus Table, fill jTextField1
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = jTable2.getSelectedRow();
                if (selectedRow != -1) {
                    // Column 0 is the Bus ID
                    jTextField1.setText(jTable2.getValueAt(selectedRow, 0).toString());
                }
            }
        });

        // When a user clicks a row in the Driver Table, fill jTextField2
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = jTable1.getSelectedRow();
                if (selectedRow != -1) {
                    // Column 0 is the Driver ID
                    jTextField2.setText(jTable1.getValueAt(selectedRow, 0).toString());
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

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jTextField4 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setLayout(null);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Bus ID", "Plate No.", "Bus Type", "Seats"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Driver ID", "Driver Name", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Search");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jLabel1.setText("Bus ID");

        jLabel2.setText("Driver ID");

        jButton1.setText("Confirm");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(773, 773, 773)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(729, 729, 729))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(65, 65, 65)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(88, 88, 88)
                                .addComponent(jButton1)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                .addGap(296, 296, 296))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
