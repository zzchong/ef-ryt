CREATE TABLE `ef_app_version_upgrade` (
  `id` CHAR(16)  NOT NULL,
  `app_id` CHAR(16)  NOT NULL DEFAULT '0' COMMENT '客户端设备id 1安卓pad 2安卓手机 3ios手机 4iospad',
  `version_id` smallint(4) unsigned DEFAULT '0' COMMENT '大版本号id',
  `version_mini` mediumint(8) unsigned DEFAULT '0' COMMENT '小版本号',
  `version_code` varchar(10) DEFAULT NULL COMMENT '版本标识 1.2',
  `type` tinyint(2) unsigned DEFAULT NULL COMMENT '是否升级  1升级，0不升级，2强制升级',
  `apk_url` varchar(255) DEFAULT NULL,
  `upgrade_point` varchar(255) DEFAULT NULL COMMENT '升级提示',
  `status` tinyint(2) DEFAULT NULL,
  `create_time` int(11) DEFAULT NULL,
  `update_time` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/**
* app表 客户端表
*/
CREATE TABLE `ef_app_info` (
  `id` CHAR(16)  NOT NULL  COMMENT '主键id',
  `name` varchar(10) DEFAULT NULL COMMENT 'APP类型名称  如 ： 安卓手机',
  `is_encryption` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否加密 1加密 0不加密',
  `key` varchar(20) NOT NULL DEFAULT '0' COMMENT '加密key',
  `create_time` int(11) NOT NULL COMMENT '创建时间',
  `update_time` int(11) NOT NULL COMMENT '更新时间',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态 1正常 0删除',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;


ALTER TABLE `app_consumer_address`
ADD COLUMN `province_str`  varchar(16) NULL AFTER `consignee`,
ADD COLUMN `city_str`  varchar(16) NULL AFTER `province_str`,
ADD COLUMN `district_str`  varchar(16) NULL AFTER `city_str`;


ALTER TABLE `app_art_work_attachment`
ADD COLUMN `status`  varchar(1) NULL AFTER `file_name`;

ALTER TABLE 'app_art_work_message_attachment'
ADD COLUMN 'video_picture' varchar(100) NULL after 'FileType';

ALTER TABLE 'app_master'
ADD COLUMN 'email' VARCHAR(50) NULL after 'phone',
ADD COLUMN 'present_city' VARCHAR(10) NULL after 'email',
ADD COLUMN 'identity_card_type' VARCHAR(10) null after 'present_city',
ADD COLUMN 'identity_card_no' VARCHAR(30) null after 'identity_card_type',
ADD COLUMN 'remark' VARCHAR(255) null after 'identity_card_no';


ALTER TABLE `app_art_work_attachment`
ADD COLUMN `width`  int(10) NULL AFTER `status`,
ADD COLUMN `height`  int(10) NULL AFTER `width`;

ALTER TABLE 'app_master_attachment'
ADD COLUMN 'type' VARCHAR(2) null after 'url',
ADD COLUMN 'width' VARCHAR(10) null after 'type',
ADD COLUMN 'height' VARCHAR(10) null after 'width';

ALTER TABLE 'app_master_work'
ADD COLUMN 'width' VARCHAR(10) null after 'type',
ADD COLUMN 'height' VARCHAR(10) null after 'width';

