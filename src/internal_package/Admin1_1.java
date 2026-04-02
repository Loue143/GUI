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

public class Admin1_1 extends javax.swing.JInternalFrame {

    /**
     * Creates new form Admin1_1
     */
    public Admin1_1() {
        initComponents();
        
        // This loads the data immediately when the window opens
        showTableData(); 
    }

    public void showTableData() {
        Users.setRowSorter(null);
    try {
        Class.forName("org.sqlite.JDBC");
        Connection con = DriverManager.getConnection("jdbc:sqlite:BBC.db");
        
        // --- MODIFICATION HERE ---
        // We add "WHERE u_type != 'Admin'" to filter them out
        String sql = "SELECT * FROM Tbl_user WHERE u_type != 'Admin' AND u_type != 'CEO'"; 
        
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Users = new javax.swing.JTable();

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Search");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
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
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setBounds(0, 0, 630, 379);
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Users;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
