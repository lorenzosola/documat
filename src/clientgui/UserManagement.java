/**
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 *  @author Lorenzo Sola: lorenzo.sola@alice.it
 */


/*
 * Created on May 10, 2005
 */
package clientgui;

import customobj.gui.TableDataManager;
import customobj.gui.TableRecord;
import customobj.wrappers.DangerousOperationException;
import customobj.wrappers.LocalProp;
import customobj.wrappers.db_con;
//import genericobj.db_con;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

/**
 * @author lsola
 */
public class UserManagement extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TableDataManager tdmUtenti;
	private JScrollPane tabellaUtenti;

	private JPanel tabellaComandi = null;
	private JPanel comandi = null;
	private JButton aggiungi = null;
	private JButton elimina = null;
	private JButton modifica = null;
	private JPanel pannelloInserimentoDatiUtente = null;
	private JPanel maskUsername = null;
	private JPanel maskIdentifier = null;
	private JPanel maskPassword = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JTextField username = null;
	private JTextField password = null;
	private JTextField identifier = null;
	private JPanel annullaSalva = null;
	private JButton applica = null;
	private JButton annulla = null;
	private TableRecord actualTableRecord;
	private JButton salva = null;
	private JPanel questo;
	private LocalProp localProp;
	private JButton AnnullaTutto = null;
	private db_con conn = null;
	private String conString, dbUser, dbPassword;
	private boolean dbOperationsEnabled = false;

	/**
	 * This is the default constructor
	 * 
	 * @throws IOException
	 */
	public UserManagement() throws IOException {
		super();
		localProp = new LocalProp("guiprop.prop");
//		this.conn = gc.getDB_con();

		try {
			if((this.dbUser = localProp.get("DBUser")) != null && (this.dbPassword = localProp.get("DBPassword")) != null && (this.conString = localProp.get("ConnString")) != null) {
				dbOperationsEnabled = true;
				conn = new db_con();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		initialize();
		questo = this;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		if(!dbOperationsEnabled) {
			JLabel notEnabled = new JLabel("Non Enabled");
			notEnabled.setHorizontalAlignment(SwingConstants.CENTER);
			notEnabled.setFont(new Font("Dialog", Font.BOLD, 15));
			Color color = new Color(120,120,120);
			notEnabled.setForeground(color);
			this.add(notEnabled, BorderLayout.CENTER);
			return;
		}
		this.add(getTabellaComandi(), java.awt.BorderLayout.CENTER);
		this.add(getPannelloInserimentoDatiUtente(), java.awt.BorderLayout.SOUTH);
	}

	/**
	 * This method initializes tabellaUtenti
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getTabellaUtenti() {
		if(tabellaUtenti == null) {
			try {
				tdmUtenti = new TableDataManager(conn, new String[] { "Username", "Whole Name", "Password" }, new String[] { "USERNAME", "IDENTIFIER", "PASSWORD"});
				try {
					tdmUtenti.init("select * from utenti", conString, dbUser, dbPassword);
				} catch (DangerousOperationException e) {
					JOptionPane.showMessageDialog(questo, e, "Warning!", JOptionPane.WARNING_MESSAGE);
				}
			}
			catch (SQLException ex) {
				JOptionPane.showMessageDialog(this, "User table view initialization not succeeded:\n" + ex.getMessage() + "\nContact the administrator.");
				return null;
			}
			tabellaUtenti = new JScrollPane();
			tabellaUtenti.setViewportView(tdmUtenti.getJTable());
		}
		return tabellaUtenti;
	}

	/**
	 * This method initializes tabellaComandi
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getTabellaComandi() {
		if(tabellaComandi == null) {
			tabellaComandi = new JPanel();
			tabellaComandi.setLayout(new BorderLayout());
			tabellaComandi.add(getComandi(), BorderLayout.SOUTH);
			tabellaComandi.add(getTabellaUtenti(), BorderLayout.CENTER);
		}
		return tabellaComandi;
	}

	/**
	 * This method initializes comandi
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getComandi() {
		if(comandi == null) {
			comandi = new JPanel();
			comandi.add(getAggiungi(), null);
			comandi.add(getElimina(), null);
			comandi.add(getModifica(), null);
			comandi.setBorder(new BevelBorder(1));
			comandi.add(getSalva(), null);
			comandi.add(getAnnullaTutto(), null);
			comandi.add(getAnnulla(), null);
		}
		return comandi;
	}

	/**
	 * This method initializes aggiungi
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAggiungi() {
		if(aggiungi == null) {
			aggiungi = new JButton("Aggiunta utente");
			aggiungi.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					actualTableRecord = tdmUtenti.addRow();
					username.setText("");
					identifier.setText("");
					password.setText("");
					comandi.setVisible(false);
					pannelloInserimentoDatiUtente.setVisible(true);
					username.setEditable(true);
				}
			});
		}
		return aggiungi;
	}

	/**
	 * This method initializes elimina
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getElimina() {
		if(elimina == null) {
			elimina = new JButton("Eliminazione utente");
			elimina.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					tdmUtenti.deleteSelectedRow();
				}

			});
		}
		return elimina;
	}

	/**
	 * This method initializes modifica
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getModifica() {
		if(modifica == null) {
			modifica = new JButton("Modifica Password");
			modifica.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					actualTableRecord = tdmUtenti.editSelectedRow();
					if(actualTableRecord!=null) {
						caricaDatiCampi(actualTableRecord);
						comandi.setVisible(false);
						pannelloInserimentoDatiUtente.setVisible(true);
						username.setEditable(false);
					}
				}
			});
		}
		return modifica;
	}

	/**
	 * This method initializes mascheraDati
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPannelloInserimentoDatiUtente() {
		if(pannelloInserimentoDatiUtente == null) {
			pannelloInserimentoDatiUtente = new JPanel(new VerticalLayout());
			pannelloInserimentoDatiUtente.setVisible(false);
			pannelloInserimentoDatiUtente.add(getMaskUsername(), null);
			pannelloInserimentoDatiUtente.add(getMaskIdentifier(), null);
			pannelloInserimentoDatiUtente.add(getMaskPassword(), null);
			pannelloInserimentoDatiUtente.add(getMaskTelephone(), null);
			pannelloInserimentoDatiUtente.add(getMaskAddress(), null);
			pannelloInserimentoDatiUtente.add(getMaskEMail(), null);
			pannelloInserimentoDatiUtente.add(getAnnullaSalva(), null);
		}
		return pannelloInserimentoDatiUtente;
	}

	/**
	 * This method initializes maskUsername
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getMaskUsername() {
		if(maskUsername == null) {
			jLabel = new JLabel("Username");
			jLabel.setPreferredSize(new Dimension(100, 21));
			maskUsername = new JPanel();
			maskUsername.add(jLabel, null);
			maskUsername.add(getUsername(), null);
		}
		return maskUsername;
	}

	/**
	 * This method initializes maskIdentifier
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getMaskIdentifier() {
		if(maskIdentifier == null) {
			jLabel1 = new JLabel("Whole Name");
			jLabel1.setPreferredSize(new Dimension(100, 21));
			maskIdentifier = new JPanel();
			maskIdentifier.add(jLabel1, null);
			maskIdentifier.add(getIdentifier(), null);
		}
		return maskIdentifier;
	}

	/**
	 * This method initializes maskPassword
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getMaskPassword() {
		if(maskPassword == null) {
			jLabel2 = new JLabel("Password");
			jLabel2.setPreferredSize(new Dimension(100, 21));
			maskPassword = new JPanel();
			maskPassword.add(jLabel2, null);
			maskPassword.add(getPassword(), null);
		}
		return maskPassword;
	}

//Inizio "template" per aggiunta dei campi nel pannello di modifica
	JPanel maskTelephone = null;
	private JPanel getMaskTelephone() {
		if(maskTelephone == null) {
			JLabel jLabel = new JLabel("Telephone");
			jLabel.setPreferredSize(new Dimension(100, 21));
			maskTelephone = new JPanel();
			maskTelephone.add(jLabel, null);
			maskTelephone.add(getTelephone(), null);
		}
		return maskTelephone;
	}
	
	JTextField telephone = null;
	private JTextField getTelephone() {
		if(telephone == null) {
			telephone = new JTextField();
			telephone.setPreferredSize(new Dimension(150, 21));

		}
		return telephone;
	}

//Fine "template" per aggiunta dei campi nel pannello di modifica

	JPanel maskAddress = null;
	private JPanel getMaskAddress() {
		if(maskAddress == null) {
			JLabel jLabel = new JLabel("Address");
			jLabel.setPreferredSize(new Dimension(100, 21));
			maskAddress = new JPanel();
			maskAddress.add(jLabel, null);
			maskAddress.add(getAddress(), null);
		}
		return maskAddress;
	}
	
	JTextField address = null;
	private JTextField getAddress() {
		if(address == null) {
			address = new JTextField();
			address.setPreferredSize(new Dimension(150, 21));

		}
		return address;
	}

	/**
	 * This method initializes username
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getUsername() {
		if(username == null) {
			username = new JTextField();
			username.setPreferredSize(new Dimension(150, 21));
		}
		return username;
	}

	/**
	 * This method initializes password
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getPassword() {
		if(password == null) {
			password = new JTextField();
			password.setPreferredSize(new Dimension(150, 21));

		}
		return password;
	}

	/**
	 * This method initializes identifier
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getIdentifier() {
		if(identifier == null) {
			identifier = new JTextField();
			identifier.setPreferredSize(new Dimension(150, 21));
		}
		return identifier;
	}


	/**
	 * This method initializes email
	 * 
	 * @return javax.swing.JTextField
	 */
	JPanel maskEMail = null;
	private JPanel getMaskEMail() {
		if(maskEMail == null) {
			JLabel jLabel = new JLabel("E-mail");
			jLabel.setPreferredSize(new Dimension(100, 21));
			maskEMail = new JPanel();
			maskEMail.add(jLabel, null);
			maskEMail.add(getEMail(), null);
		}
		return maskEMail;
	}
	
	JTextField email = null;
	private JTextField getEMail() {
		if(email == null) {
			email = new JTextField();
			email.setPreferredSize(new Dimension(150, 21));

		}
		return email;
	}

	/**
	 * This method initializes annullaSalva
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAnnullaSalva() {
		if(annullaSalva == null) {
			annullaSalva = new JPanel();
			annullaSalva.add(getApplica(), null);
			annullaSalva.add(getAnnulla(), null);
			annullaSalva.setBorder(new BevelBorder(1));
		}
		return annullaSalva;
	}

	/**
	 * This method initializes applica
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getApplica() {
		if(applica == null) {
			applica = new JButton("Applica");
			applica.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(username == null || username.getText().length() == 0 || password == null || password.getText().length() == 0) {
						JOptionPane.showMessageDialog(questo, "Inserire almeno username e password.");
						return;
					}
					if(actualTableRecord.getStatus() == TableRecord.STATO_MODIFICATO) {
						actualTableRecord.addField("OLDPASSWORD", actualTableRecord.getField("PASSWORD"));// Conservo la vecchia password.
					}
					else if(actualTableRecord.getStatus() == TableRecord.STATO_NUOVO) {
							actualTableRecord.setField("USERNAME", username.getText().toUpperCase());
					}
					actualTableRecord.setField("IDENTIFIER", identifier.getText());
					actualTableRecord.setField("PASSWORD", password.getText());
					try {
						if(telephone != null && telephone.getText().length() != 0) {
							Integer.parseInt(telephone.getText());
							actualTableRecord.setField("TELEFONO", telephone.getText());
						}
						else {
							actualTableRecord.setField("TELEFONO", null);
						}
					}
					catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(questo, "Formato del numero di telefono non valido");
						return;
					}
					actualTableRecord.setField("INDIRIZZO", address.getText());
					actualTableRecord.setField("email", email.getText());
					tdmUtenti.synchronizeTable(actualTableRecord.getAccessKey());
					pannelloInserimentoDatiUtente.setVisible(false);
					comandi.setVisible(true);
				}
			});
		}
		return applica;
	}

	/**
	 * This method initializes annulla
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAnnulla() {
		if(annulla == null) {
			annulla = new JButton("Annulla");
			annulla.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(actualTableRecord != null)
						if(actualTableRecord.getStatus() == TableRecord.STATO_NUOVO) tdmUtenti.deleteRowByKey(actualTableRecord.getAccessKey());
						else actualTableRecord.setStatus(TableRecord.STATO_INIZIALE);
					pannelloInserimentoDatiUtente.setVisible(false);
					comandi.setVisible(true);
				}
			});
		}
		return annulla;
	}

	private void caricaDatiCampi(TableRecord tb) {
		if(tb != null) {
			username.setText(tb.getField("USERNAME").toString());
			identifier.setText(tb.getField("IDENTIFIER") != null ? tb.getField("IDENTIFIER").toString() : null);
			password.setText(tb.getField("PASSWORD").toString());
			telephone.setText(tb.getField("TELEFONO") != null ? tb.getField("TELEFONO").toString() : null);
			address.setText(tb.getField("INDIRIZZO") != null ? tb.getField("INDIRIZZO").toString() : null);
			email.setText(tb.getField("email") != null ? tb.getField("email").toString() : null);
		}
	}

	/**
	 * This method initializes salva
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getSalva() {
		if(salva == null) {
			salva = new JButton("Salva modifiche");
			salva.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						tdmUtenti.updateDB("utenti", new String[] {"USERNAME", "IDENTIFIER", "PASSWORD", "TELEFONO", "INDIRIZZO", "email"}, new String[] {"ID"});
						
					}
					catch (SQLException e1) {
						JOptionPane.showMessageDialog(questo, e1.getMessage());
						e1.printStackTrace();
						return;
					}
				}
			});
		}
		return salva;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAnnullaTutto() {
		if(AnnullaTutto == null) {
			AnnullaTutto = new JButton();
			AnnullaTutto.setText("Annulla");
			AnnullaTutto.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						try {
							tdmUtenti.reinit();
						} catch (DangerousOperationException e1) {
							JOptionPane.showMessageDialog(questo, e1, "Warning!", JOptionPane.WARNING_MESSAGE);
						} //Per ora si rifa` la query. Con piu` tempo si puo` pensare di modificare il TableDataManager in modo che le righe modificate vengano restituite al modificatore in nuovi TableRecord copie di quelli originali ma classificati come MODIFIED mentre i primi vengono classificati come OLD (in modo da tenerli in memoria) per costruire una funzione di ripristino che non effettui nessuna query. Ho gia` abbozzato qualcosa ma ovviamente manca il tempo quindi e` commentata.  
					}
					catch (SQLException e1) {
						JOptionPane.showMessageDialog(questo, "BD Problems: contact the administrator");
						e1.printStackTrace();
					}
				}
			});
		}
		return AnnullaTutto;
	}

	/*
	 * private JTextField getJTextField() { if(jTextField == null) { jTextField = new JTextField(); jTextField.setPreferredSize(new Dimension(150, 21)); } return jTextField; }
	 */
}