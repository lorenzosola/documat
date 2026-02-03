# --------------------------------------------------------
# Host:                         10.201.5.192
# Server version:               5.1.49-3
# Server OS:                    debian-linux-gnu
# HeidiSQL version:             6.0.0.3603
# Date/time:                    2011-09-01 10:43:15
# --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

# Dumping database structure for documat
DROP DATABASE IF EXISTS `documat`;
CREATE DATABASE IF NOT EXISTS `documat` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `documat`;


# Dumping structure for table documat.attivita
DROP TABLE IF EXISTS `attivita`;
CREATE TABLE IF NOT EXISTS `attivita` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `descrizione` mediumtext NOT NULL COMMENT 'Descrizione attività in formato testo.',
  `periodo` smallint(6) NOT NULL COMMENT 'Intervallo periodico in giorni entro il quale va ripetuta l''attività',
  `ultima` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT 'Ultimo istante in cui l''attività è stata svolta',
  `avviso_inviato` char(1) DEFAULT NULL COMMENT 'Può essere 1, 0 o null (equivalente a 0). Deve essere posto a 0 ogni volta che si aggiorna il campo "ultima" ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table documat.attivita_elementi
DROP TABLE IF EXISTS `attivita_elementi`;
CREATE TABLE IF NOT EXISTS `attivita_elementi` (
  `id_attivita` int(10) NOT NULL,
  `nomefile` varchar(200) NOT NULL,
  `titolo` varchar(200) NOT NULL,
  `owner_id` int(10) NOT NULL,
  PRIMARY KEY (`id_attivita`,`nomefile`,`titolo`,`owner_id`),
  KEY `FK_attivita_elementi_fileinfo` (`nomefile`),
  KEY `FK_attivita_elementi_fileinfo_2` (`titolo`),
  KEY `FK_attivita_elementi_fileinfo_3` (`owner_id`),
  CONSTRAINT `FK_attivita_elementi_attivita` FOREIGN KEY (`id_attivita`) REFERENCES `attivita` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_attivita_elementi_fileinfo` FOREIGN KEY (`nomefile`) REFERENCES `fileinfo` (`nomefile`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_attivita_elementi_fileinfo_2` FOREIGN KEY (`titolo`) REFERENCES `fileinfo` (`descrizione`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_attivita_elementi_fileinfo_3` FOREIGN KEY (`owner_id`) REFERENCES `fileinfo` (`owner_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table documat.attivita_utenti
DROP TABLE IF EXISTS `attivita_utenti`;
CREATE TABLE IF NOT EXISTS `attivita_utenti` (
  `id_attivita` int(10) NOT NULL,
  `id_utente` int(10) NOT NULL,
  PRIMARY KEY (`id_attivita`,`id_utente`),
  KEY `FK_attivuta_utenti_utenti` (`id_utente`),
  CONSTRAINT `FK_attivuta_utenti_attivita` FOREIGN KEY (`id_attivita`) REFERENCES `attivita` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_attivuta_utenti_utenti` FOREIGN KEY (`id_utente`) REFERENCES `utenti` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for procedure documat.clear_db
DROP PROCEDURE IF EXISTS `clear_db`;
DELIMITER //
CREATE DEFINER=`admin`@`%` PROCEDURE `clear_db`()
begin
	delete from attivita_elementi;
	delete from attivita_utenti;
	delete from attivita;
	delete from grp_cncpt_files;
	delete from file_sig_words;
	delete from grp_cncpt_words;
	delete from grp_cncpt;
	delete from filters_descr;
	delete from filters_words;
	delete from fileinfo;
	delete from lastpath;
	delete from utenti;
end//
DELIMITER ;


# Dumping structure for procedure documat.demoSp
DROP PROCEDURE IF EXISTS `demoSp`;
DELIMITER //
CREATE DEFINER=`admin`@`%` PROCEDURE `demoSp`(IN inputParam VARCHAR(255), INOUT inOutParam INT)
BEGIN
    DECLARE z INT;
    SET z = inOutParam + 1;
    SET inOutParam = z;

    SELECT inputParam;

    SELECT CONCAT('zyxw', inputParam);
END//
DELIMITER ;


# Dumping structure for table documat.fileinfo
DROP TABLE IF EXISTS `fileinfo`;
CREATE TABLE IF NOT EXISTS `fileinfo` (
  `id` int(10) NOT NULL,
  `descrizione` varchar(250) NOT NULL COMMENT 'Qui viene immessa una breve descrizione oppure il titolo del documento (uso piu` frequente).',
  `data` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `versione` smallint(6) NOT NULL,
  `nomefile` varchar(100) NOT NULL,
  `estensione` varchar(12) NOT NULL,
  `arch_path` varchar(100) NOT NULL,
  `language_filter` int(11) NOT NULL,
  `orig_path` varchar(300) DEFAULT NULL,
  `owner_id` int(10) NOT NULL,
  `insertion_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`),
  KEY `ouner_id_user_id` (`owner_id`),
  KEY `fileinfonewindex` (`descrizione`),
  KEY `indexnomi` (`nomefile`),
  KEY `FK_fileinfo_filters_descr` (`language_filter`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table documat.file_sig_words
DROP TABLE IF EXISTS `file_sig_words`;
CREATE TABLE IF NOT EXISTS `file_sig_words` (
  `file_id` int(11) NOT NULL DEFAULT '0',
  `word_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`file_id`,`word_id`),
  KEY `file_sig_words_grp_cncpt_words` (`word_id`),
  CONSTRAINT `file_sig_words_fileinfo` FOREIGN KEY (`file_id`) REFERENCES `fileinfo` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `file_sig_words_grp_cncpt_words` FOREIGN KEY (`word_id`) REFERENCES `grp_cncpt_words` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table documat.filters_descr
DROP TABLE IF EXISTS `filters_descr`;
CREATE TABLE IF NOT EXISTS `filters_descr` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `descr` varchar(40) NOT NULL,
  `user` int(11) NOT NULL,
  `linguistic` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_filters_descr` (`user`,`descr`),
  CONSTRAINT `filters_descr_utenti` FOREIGN KEY (`user`) REFERENCES `utenti` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table documat.filters_words
DROP TABLE IF EXISTS `filters_words`;
CREATE TABLE IF NOT EXISTS `filters_words` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `descr` int(10) NOT NULL,
  `word` varchar(40) NOT NULL,
  `relevance` float NOT NULL COMMENT 'Questa per ora non rilevante (il SW la imposta ad 1 all'' inizializzazione delle FW e non la cambia mai).',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_descr_word` (`descr`,`word`),
  CONSTRAINT `filters_words_filters_descr` FOREIGN KEY (`descr`) REFERENCES `filters_descr` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table documat.grp_cncpt
DROP TABLE IF EXISTS `grp_cncpt`;
CREATE TABLE IF NOT EXISTS `grp_cncpt` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `descr` varchar(80) NOT NULL,
  `user` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `grp_cncpt_utenti` (`user`),
  CONSTRAINT `grp_cncpt_utenti` FOREIGN KEY (`user`) REFERENCES `utenti` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table documat.grp_cncpt_files
DROP TABLE IF EXISTS `grp_cncpt_files`;
CREATE TABLE IF NOT EXISTS `grp_cncpt_files` (
  `grp_cncpt` int(10) NOT NULL,
  `file` int(10) NOT NULL,
  PRIMARY KEY (`grp_cncpt`,`file`),
  KEY `grp_cncpt_files_fileinfo` (`file`),
  CONSTRAINT `grp_cncpt_files_fileinfo` FOREIGN KEY (`file`) REFERENCES `fileinfo` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `grp_cncpt_files_grp_cncpt` FOREIGN KEY (`grp_cncpt`) REFERENCES `grp_cncpt` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for table documat.grp_cncpt_words
DROP TABLE IF EXISTS `grp_cncpt_words`;
CREATE TABLE IF NOT EXISTS `grp_cncpt_words` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `grp_cncpt` int(10) NOT NULL,
  `word` varchar(40) NOT NULL,
  `relevance` float NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniqueness_grp_id_word` (`grp_cncpt`,`word`),
  CONSTRAINT `grp_cncpt_words_grp_cncpt` FOREIGN KEY (`grp_cncpt`) REFERENCES `grp_cncpt` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for function documat.inser_ass_file_sigwords
DROP FUNCTION IF EXISTS `inser_ass_file_sigwords`;
DELIMITER //
CREATE DEFINER=`admin`@`%` FUNCTION `inser_ass_file_sigwords`(`_groupid` INTEGER, `_fileid` INTEGER, `_sigword` VARCHAR(100)) RETURNS int(11) DETERMINISTIC
begin
	declare _wordid, _nrecord integer;
	select gcw.id from grp_cncpt_words gcw
	where gcw.grp_cncpt = _groupid and gcw.word = _sigword and gcw.id not in
		(select gcw.id from file_sig_words fsw
		where fsw.file_id = _fileid and fsw.word_id = gcw.id) into _wordid;
	if(_wordid is not null) then
	set _nrecord = 0;
	begin
		insert into file_sig_words(file_id,word_id)
		values(_fileid,_wordid);
		set _nrecord = 1;
	end;
	end if;
	return _nrecord;
end//
DELIMITER ;


# Dumping structure for table documat.lastpath
DROP TABLE IF EXISTS `lastpath`;
CREATE TABLE IF NOT EXISTS `lastpath` (
  `path1` int(10) NOT NULL,
  `path2` int(10) NOT NULL,
  `path3` int(10) NOT NULL,
  `path4` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.


# Dumping structure for procedure documat.update_sig_word
DROP PROCEDURE IF EXISTS `update_sig_word`;
DELIMITER //
CREATE DEFINER=`admin`@`%` PROCEDURE `update_sig_word`(IN `_grp_cncpt` INT, IN `_word` VARCHAR(100), IN `_added_relevance` FLOAT, IN `_with_insert` CHAR(1), OUT `_added_records` INT, OUT `_updated_records` INT)
BEGIN
	declare _id integer;
	set _id = null;
	set _added_records = 0;
	set _updated_records = 0;
	select id from grp_cncpt_words where grp_cncpt = _grp_cncpt
	and word = _word
	into _id;
	if(_id is not null) then
	begin
		update grp_cncpt_words set relevance = relevance + _added_relevance
		where grp_cncpt = _grp_cncpt and word = _word;
		set _updated_records = 1;
	end;
	elseif (_with_insert = '1') then
	begin
		insert into grp_cncpt_words(grp_cncpt, word, relevance) values (_grp_cncpt, _word, _added_relevance);
		set _added_records = 1;
	end;
	end if;
END//
DELIMITER ;


# Dumping structure for table documat.utenti
DROP TABLE IF EXISTS `utenti`;
CREATE TABLE IF NOT EXISTS `utenti` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `username` varchar(40) NOT NULL,
  `identifier` varchar(40) DEFAULT NULL COMMENT 'E'' l''identificativo (es. Nome Cognome), non lo "username" utilizzato per l''accesso. Verificae se può essere not null.',
  `password` varchar(40) NOT NULL,
  `telefono` decimal(20,0) unsigned DEFAULT NULL,
  `indirizzo` varchar(200) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_username` (`identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Data exporting was unselected.
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
