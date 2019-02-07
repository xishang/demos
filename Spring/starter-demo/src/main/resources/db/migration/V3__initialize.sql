ALTER TABLE `e_mall`.`user`
CHANGE COLUMN `password` `password` VARCHAR(16) NOT NULL COMMENT '用户密码' ;

INSERT INTO user (username, password, name, phone, create_time) VALUES ('admin', 'admin', '管理员', '18812345678', now());