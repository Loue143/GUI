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
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Dell
 */
public class Super_Admin2_8 extends javax.swing.JInternalFrame {

    private final String url = "jdbc:sqlite:BBC.db"; 
    
    // Global variable to hold the ID of the currently selected destination
    private String selectedDestId = null;
    /**
     * Creates new form Super_Admin2_8
     */
    public Super_Admin2_8() {
        initComponents();
        customInit();
    }
    
    private void customInit() {
        // Group the radio buttons so only one can be selected
        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add(jRadioButton1); // Re-Open
        bg.add(jRadioButton2); // Close

        // 1. Add Mouse Listener to the JTable to populate fields when clicked
        Users.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked();
            }
        });
        
        // 2. Real-time Search Listener for the Search Bar
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                applyClientSearch();
            }
        });
        
        // 3. Load initial data into the table
        fetchData();
    }

    // Method to Fetch Data from Database and populate the JTable
    private void fetchData() {
    try (Connection conn = DriverManager.getConnection(url)) {
        // We select exactly 5 columns
        String sql = "SELECT De_ID, Name, time, KM, status FROM tbl_destinations";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        
        DefaultTableModel model = (DefaultTableModel) Users.getModel();
        model.setRowCount(0); 
        
        while (rs.next()) {
            Object[] row = new Object[5]; // Size is 5 because we have 5 columns
            row[0] = rs.getInt("De_ID");    // ID
            row[1] = rs.getString("Name");  // Name
            row[2] = rs.getString("time");  // Time
            row[3] = rs.getString("KM");    // KM
            row[4] = rs.getString("status"); // Status - This is Index 4
            model.addRow(row);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
    }
}

    // Filters the loaded table data locally based on the search bar
    private void applyClientSearch() {
        DefaultTableModel model = (DefaultTableModel) Users.getModel();
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        Users.setRowSorter(trs);
        
        String query = jTextField1.getText();
        trs.setRowFilter(RowFilter.regexFilter("(?i)" + query)); // (?i) makes it case-insensitive
    }

    // Method executed when a row is clicked
    private void tableMouseClicked() {
    DefaultTableModel model = (DefaultTableModel) Users.getModel();
    int viewRow = Users.getSelectedRow();
    if (viewRow != -1) {
        int modelRow = Users.convertRowIndexToModel(viewRow);
        
        selectedDestId = model.getValueAt(modelRow, 0).toString();
        
        // Populate Text Fields - Adjusted Indices
        jTextField2.setText(model.getValueAt(modelRow, 1).toString()); // Name
        jTextField5.setText(model.getValueAt(modelRow, 2).toString()); // Time
        jTextField6.setText(model.getValueAt(modelRow, 3).toString()); // KM
        
        // Status logic - Adjusted Index to 4
        String status = model.getValueAt(modelRow, 4) != null ? model.getValueAt(modelRow, 4).toString() : "";
        if (status.equalsIgnoreCase("Closed")) {
            jRadioButton2.setSelected(true); 
        } else {
            jRadioButton1.setSelected(true); // Re-Open/Available
        }
    }
}

    // Method to execute the UPDATE statement
    private void updateDestinationRecord() {
    if (selectedDestId == null) {
        JOptionPane.showMessageDialog(this, "Please select a destination from the table first.");
        return;
    }

    String name = jTextField2.getText();
    String time = jTextField5.getText();
    String km = jTextField6.getText();
    
    if (name.isEmpty() || time.isEmpty() || km.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please ensure all fields are filled out.");
        return;
    }

    // Determine new status based on radio buttons
    String newStatus = jRadioButton1.isSelected() ? "Available" : "Closed";

    try (Connection conn = DriverManager.getConnection(url)) {
        // The SQL has 5 parameters (?)
        String sql = "UPDATE tbl_destinations SET Name = ?, time = ?, KM = ?, status = ? WHERE De_ID = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        
        pstmt.setString(1, name);
        pstmt.setString(2, time);      // Index 2 is Time
        pstmt.setString(3, km);        // Index 3 is KM
        pstmt.setString(4, newStatus); // Index 4 is Status
        pstmt.setInt(5, Integer.parseInt(selectedDestId)); // Index 5 is the WHERE ID
        
        pstmt.executeUpdate();
        
        JOptionPane.showMessageDialog(this, "Destination Record Updated Successfully!");
        
        fetchData(); // Refresh the table
        clearFields(); // Helper to clear inputs
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error updating record: " + e.getMessage());
    }
}

// Simple helper to keep code clean
private void clearFields() {
    selectedDestId = null;
    jTextField2.setText("");
    jTextField5.setText("");
    jTextField6.setText("");
    Users.clearSelection();
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField4 = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Users = new javax.swing.JTable();
        jTextField2 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();

        jTextField4.setText("jTextField2");

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

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
                "ID", "Name", "Time", "KM", "Status"
            }
        ));
        jScrollPane1.setViewportView(Users);

        jLabel2.setText("Name");

        jLabel4.setText("Time");

        jLabel5.setText("KM");

        jLabel7.setText("----------------");

        jLabel8.setText("----------------");

        jLabel9.setText("Or");

        jButton1.setText("Update");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jRadioButton1.setText("Re-Open");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jRadioButton2.setText("Close");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel9))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jRadioButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jRadioButton1))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(58, 58, 58)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(15, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel7))
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48))))
        );

        setBounds(0, 0, 601, 379);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        String searchString = jTextField1.getText();

        DefaultTableModel model = (DefaultTableModel) Users.getModel();

        // 2. Create the sorter
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        Users.setRowSorter(trs);

        // 3. Get text from search bar and apply filter
        // "(?i)" makes the search case-insensitive
        String query = jTextField1.getText();
        trs.setRowFilter(RowFilter.regexFilter("(?i)" + query));
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int selectedRow = Users.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a destination first.");
        return;
    }

    int modelRow = Users.convertRowIndexToModel(selectedRow);
    String destId = Users.getModel().getValueAt(modelRow, 0).toString();

    String name = jTextField2.getText();
    String time = jTextField5.getText();
    String km = jTextField6.getText();
    
    if (name.isEmpty() || time.isEmpty() || km.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in Name, Time, and KM.");
        return;
    }

    // Determine status
    String newStatus = jRadioButton2.isSelected() ? "Closed" : "Available";

    try (Connection conn = DriverManager.getConnection(url)) {
        // Removed Sched = ? from the SQL
        String sql = "UPDATE tbl_destinations SET Name = ?, time = ?, KM = ?, status = ? WHERE De_ID = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        
        pstmt.setString(1, name);
        pstmt.setString(2, time);
        pstmt.setString(3, km);
        pstmt.setString(4, newStatus);
        pstmt.setInt(5, Integer.parseInt(destId)); // This is now index 5
        
        pstmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "Updated Successfully!");
        
        fetchData(); // Refresh table
        
        // Clear inputs
        jTextField2.setText("");
        jTextField5.setText("");
        jTextField6.setText("");
        Users.clearSelection();
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Users;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
}
