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
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Dell
 */
public class Super_Admin2_5 extends javax.swing.JInternalFrame {

    
    private final String url = "jdbc:sqlite:BBC.db"; 
    
    // Global variable to hold the ID of the currently selected bus
    private String selectedBusId = null;
    
    // Button groups to ensure only one radio button is selected at a time
    private ButtonGroup bgBusType;
    private ButtonGroup bgStatus;
    /**
     * Creates new form Super_Admin2_5
     */
    public Super_Admin2_5() {
        initComponents();
        customInit();
    }
    
    private void customInit() {
        // 1. Group the Bus Type Radio Buttons
        bgBusType = new ButtonGroup();
        bgBusType.add(jRadioButton3); // Regular
        bgBusType.add(jRadioButton2); // Aircon
        
        // 2. Group the Status Radio Buttons
        bgStatus = new ButtonGroup();
        bgStatus.add(jRadioButton4); // Pull out
        bgStatus.add(jRadioButton1); // Re-Deploy
        
        // Disable status buttons by default until a row is clicked
        jRadioButton4.setEnabled(false);
        jRadioButton1.setEnabled(false);
        
        // 3. Add Mouse Listener to the JTable
        Users.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked();
            }
        });
        
        // 4. Add Key Listener to the Search Field for real-time filtering
        jTextField6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fetchData(jTextField6.getText());
            }
        });
        
        // 5. Load initial data into the table
        fetchData("");
    }
    
    private void fetchData(String searchTerm) {
        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "SELECT Bus_ID, Plate_No, Bus_type, seats, Status FROM tbl_bus WHERE Plate_No LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + searchTerm + "%"); // Allows partial matching
            
            ResultSet rs = pstmt.executeQuery();
            
            // Get the table model and clear existing rows
            DefaultTableModel model = (DefaultTableModel) Users.getModel();
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = new Object[5];
                row[0] = rs.getInt("Bus_ID");
                row[1] = rs.getString("Plate_No");
                row[2] = rs.getString("Bus_type");
                row[3] = rs.getInt("seats");
                row[4] = rs.getString("Status");
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }
    
    private void tableMouseClicked() {
        DefaultTableModel model = (DefaultTableModel) Users.getModel();
        int selectedRow = Users.getSelectedRow();
        
        if (selectedRow != -1) {
            // Get the Bus_ID
            selectedBusId = model.getValueAt(selectedRow, 0).toString();
            
            // Populate Text Fields
            jTextField1.setText(model.getValueAt(selectedRow, 1) != null ? model.getValueAt(selectedRow, 1).toString() : ""); // Plate No
            jTextField4.setText(model.getValueAt(selectedRow, 3) != null ? model.getValueAt(selectedRow, 3).toString() : ""); // Seats
            
            // Populate Bus Type Radio Buttons
            String type = model.getValueAt(selectedRow, 2) != null ? model.getValueAt(selectedRow, 2).toString() : "";
            if (type.equalsIgnoreCase("Regular")) {
                jRadioButton3.setSelected(true);
            } else if (type.equalsIgnoreCase("Aircon")) {
                jRadioButton2.setSelected(true);
            } else {
                bgBusType.clearSelection();
            }
            
            // Apply Logic for Status Radio Buttons based on database value
            String status = model.getValueAt(selectedRow, 4) != null ? model.getValueAt(selectedRow, 4).toString() : "";
            bgStatus.clearSelection(); // Clear previous selections
            
            if (status.equalsIgnoreCase("Deployed")) {
                jRadioButton4.setEnabled(true);   // Can select "Pull out"
                jRadioButton1.setEnabled(false);  // Cannot select "Re-Deploy" because it is already deployed
            } else if (status.equalsIgnoreCase("Inactive")) {
                jRadioButton4.setEnabled(false);  // Cannot select "Pull out" because it's already inactive
                jRadioButton1.setEnabled(true);   // Can select "Re-Deploy"
            } else {
                // Fallback for NULL or empty statuses
                jRadioButton4.setEnabled(true);
                jRadioButton1.setEnabled(true);
            }
        }
    }
    
    private void updateBusRecord() {
        if (selectedBusId == null) {
            JOptionPane.showMessageDialog(this, "Please select a bus from the table first.");
            return;
        }

        String plateNo = jTextField1.getText();
        String seats = jTextField4.getText();
        
        String busType = "";
        if (jRadioButton3.isSelected()) busType = "Regular";
        else if (jRadioButton2.isSelected()) busType = "Aircon";

        // Determine new status based on user action
        String newStatus = ""; 
        if (jRadioButton4.isSelected()) {
            newStatus = "Inactive"; // "Pull out" changes status to Inactive
        } else if (jRadioButton1.isSelected()) {
            newStatus = "Deployed"; // "Re-Deploy" changes status to Deployed
        } else {
            // If they didn't click either, retain the current status from the table
            int row = Users.getSelectedRow();
            newStatus = Users.getValueAt(row, 4) != null ? Users.getValueAt(row, 4).toString() : "";
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            String sql = "UPDATE tbl_bus SET Plate_No = ?, Bus_type = ?, seats = ?, Status = ? WHERE Bus_ID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, plateNo);
            pstmt.setString(2, busType);
            pstmt.setInt(3, Integer.parseInt(seats));
            pstmt.setString(4, newStatus);
            pstmt.setInt(5, Integer.parseInt(selectedBusId));
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Bus Record Updated Successfully!");
            
            // Refresh table and clear selections
            fetchData("");
            selectedBusId = null;
            jTextField1.setText("");
            jTextField4.setText("");
            bgBusType.clearSelection();
            bgStatus.clearSelection();
            jRadioButton4.setEnabled(false);
            jRadioButton1.setEnabled(false);
            
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Seat capacity must be a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating record: " + e.getMessage());
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

        jTextField3 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jTextField6 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Users = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();

        jTextField3.setText("jTextField1");

        jLabel7.setText("Plate No.");

        jLabel9.setText("Plate No.");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setText("-------------");

        jLabel2.setText("-------------");

        jLabel3.setText("Or");

        jPanel7.setBackground(new java.awt.Color(0, 0, 0));

        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Search");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(391, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

        jLabel5.setText("Plate No.");

        jLabel8.setText("Bus Type");

        jButton1.setText("Confirm");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jRadioButton2.setText("Aircon");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jRadioButton1.setText("Re-Deploy");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jRadioButton3.setText("Regular");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        jRadioButton4.setText("Pull out");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jRadioButton3)
                                .addGap(18, 18, 18)
                                .addComponent(jRadioButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(2, 2, 2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jRadioButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioButton1)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton4))
                .addGap(27, 27, 27)
                .addComponent(jButton1)
                .addContainerGap(38, Short.MAX_VALUE))
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
                "ID", "Plate No.", "Bus Type", "Seat Capacity", "Status"
            }
        ));
        jScrollPane1.setViewportView(Users);

        jLabel6.setText("Seat Capacity");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(jLabel6)
                .addContainerGap(276, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setBounds(0, 0, 601, 379);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int selectedRow = Users.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bus from the table first before confirming.");
            return;
        }

        // Get the Bus_ID from the selected row (Column 0)
        String selectedBusId = Users.getValueAt(selectedRow, 0).toString();

        // 2. Gather the updated data from your text fields
        String plateNo = jTextField1.getText();
        String seats = jTextField4.getText();
        
        // Gather the updated Bus Type
        String busType = "";
        if (jRadioButton3.isSelected()) {
            busType = "Regular";
        } else if (jRadioButton2.isSelected()) {
            busType = "Aircon";
        }

        // 3. Apply the Status Logic based on user selection
        String newStatus = ""; 
        boolean isPullingOut = false; // Flag to track if we are setting it to Inactive

        if (jRadioButton4.isSelected()) {
            newStatus = "Inactive"; // "Pull out" changes status to Inactive
            isPullingOut = true;    // Set flag to true
        } else if (jRadioButton1.isSelected()) {
            newStatus = "Deployed"; // "Re-Deploy" changes status to Deployed
        } else {
            // If the user didn't change the status, keep the current one from the table
            newStatus = Users.getValueAt(selectedRow, 4) != null ? Users.getValueAt(selectedRow, 4).toString() : "";
        }

        // Basic validation
        if (plateNo.isEmpty() || seats.isEmpty() || busType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please ensure Plate No, Seat Capacity, and Bus Type are filled out.");
            return;
        }

        // 4. Update the Database
        try {
            String url = "jdbc:sqlite:BBC.db"; 
            Connection conn = DriverManager.getConnection(url);
            
            String sql;
            // Dynamically build the SQL statement based on the pull-out status
            if (isPullingOut) {
                // If pulling out, set dr_ID and De_ID to NULL in addition to other updates
                sql = "UPDATE tbl_bus SET Plate_No = ?, Bus_type = ?, seats = ?, Status = ?, dr_ID = NULL, De_ID = NULL WHERE Bus_ID = ?";
            } else {
                // Standard update
                sql = "UPDATE tbl_bus SET Plate_No = ?, Bus_type = ?, seats = ?, Status = ? WHERE Bus_ID = ?";
            }
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, plateNo);
            pstmt.setString(2, busType);
            pstmt.setInt(3, Integer.parseInt(seats)); // Ensure seats is parsed as an integer
            pstmt.setString(4, newStatus);
            pstmt.setInt(5, Integer.parseInt(selectedBusId)); // Match exactly which bus to update
            
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Bus Record Updated Successfully!");
            
            pstmt.close();
            conn.close();
            
            // 5. Refresh the UI
            fetchData(""); // Refresh the JTable 
            
            // Clear inputs so it's ready for the next action
            jTextField1.setText("");
            jTextField4.setText("");
            selectedBusId = null; // Clear the selected ID globally
            
            // Uncheck the radio buttons
            if(bgBusType != null) bgBusType.clearSelection(); 
            if(bgStatus != null) bgStatus.clearSelection();  
            
            jRadioButton4.setEnabled(false);
            jRadioButton1.setEnabled(false);
            Users.clearSelection(); // Deselect the row in the table
            
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Seat capacity must be a valid number.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating record: " + e.getMessage());
        }
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Users;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
}
