mdmp-sdk
========

This is the SDK for mdmp


CREATE TABLE `mdmp` (
  `resumeTime` varchar(20) NOT NULL,
  `appKey` varchar(20) NOT NULL DEFAULT '',
  `osType` varchar(10) NOT NULL DEFAULT '',
  `category` varchar(30) DEFAULT '',
  `action` varchar(20) DEFAULT '',
  `devId` varchar(30) DEFAULT '',
  `value` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`resumeTime`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='latin1_swedish_ci'
