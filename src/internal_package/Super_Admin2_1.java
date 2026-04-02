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
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Dell
 */

public class Super_Admin2_1 extends javax.swing.JInternalFrame {
private javax.swing.JDesktopPane desktop;
    /**
   
     */
    public Super_Admin2_1() {
        initComponents();
        showTableData();
    }

    public void showTableData() {
        Users.setRowSorter(null);
    try {
        Class.forName("org.sqlite.JDBC");
        Connection con = DriverManager.getConnection("jdbc:sqlite:BBC.db");
        
        // --- MODIFICATION HERE ---
        // We add "WHERE u_type != 'Admin'" to filter them out
        String sql = "SELECT * FROM Tbl_user WHERE u_type != 'User'"; 
        
        
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        
        DefaultTableModel model = (DefaultTableModel) Users.getModel();
        model.setRowCount(0);
        
        while(rs.next()) {
            String id = String.valueOf(rs.getInt("userid"));
            String fname = rs.getString("f_name");
            String lname = rs.getString("l_name");
            String email = rs.getString("email");
            String uname = rs.getString("username");
            String pass  = rs.getString("password");
            String stat  = rs.getString("status");
            String acc   = rs.getString("u_type");
            
            String[] tbData = {id, fname, lname, email, uname, pass, stat, acc};
            model.addRow(tbData);
        }
        
        rs.close();
        pst.close();
        con.close();
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel7 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Users = new javax.swing.JTable();

        jPanel7.setBackground(new java.awt.Color(0, 0, 0));

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Search");

        jToggleButton1.setText("Add Admin");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(243, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jToggleButton1))
                .addContainerGap())
        );

        Users.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "First Name", "Last Name", "Email", "Username", "Password", "Status", "Account"
            }
        ));
        jScrollPane1.setViewportView(Users);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(83, 83, 83))
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

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        Super_Admin2_2 addAdminFrame = new Super_Admin2_2();
    
    // 2. Find the DesktopPane this frame is currently sitting in
    javax.swing.JDesktopPane desktopPane = getDesktopPane();
    
    if (desktopPane != null) {
        // 3. Add the new frame to the desktop
        desktopPane.add(addAdminFrame);
        addAdminFrame.setVisible(true);
        
        // 4. Optional: Close the current list frame if you want to switch views
        // this.dispose(); 
    } else {
        JOptionPane.showMessageDialog(this, "Error: Could not find Desktop Pane.");
    }
    }//GEN-LAST:event_jToggleButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Users;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables
}
