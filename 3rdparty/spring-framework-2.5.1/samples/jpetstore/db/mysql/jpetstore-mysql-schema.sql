use jpetstore;

create table if not exists supplier (
   suppid int not null, 
    name varchar(80) null,
    status varchar(2) not null,
    addr1 varchar(80) null,
    addr2 varchar(80) null,
    city varchar(80) null,
    state varchar(80) null,
    zip varchar(5) null,
    phone varchar(80) null,  
primary key (suppid)) 
type=innodb 
min_rows=0 
max_rows=1000 
pack_keys=default 
row_format=default 
comment='cadastro de fornecedores';

create table if not exists signon (
    username varchar(25) not null,
    password varchar(25)  not null,  
primary key (username)) 
type=innodb 
min_rows=0 
max_rows=1000  
pack_keys=default 
row_format=default 
comment='cadastro de usuários';

create table if not exists account (
    userid varchar(80) not null,
    email varchar(80) not null,
    firstname varchar(80) not null,
    lastname varchar(80) not null,
    status varchar(2)  null,
    addr1 varchar(80) not null,
    addr2 varchar(40) null,
    city varchar(80) not  null,
    state varchar(80) not null,
    zip varchar(20) not null,
    country varchar(20) not null,
    phone varchar(80) not null,
primary key (userid) )
type=innodb
min_rows=0 
max_rows=1000  
pack_keys=default 
row_format=default 
comment='cadastro de contas';

create table if not exists profile (
    userid varchar(80) not null,
    langpref varchar(80) not null,
    favcategory varchar(30),
    mylistopt bool,
    banneropt bool,
primary key (userid) )
type=innodb 
pack_keys=default 
row_format=default 
comment='cadastro de perfis';

create table if not exists bannerdata (
    favcategory varchar(80) not null,
    bannername varchar(255)  null, 
primary key (favcategory))
type=innodb 
pack_keys=default 
row_format=default 
comment='banner data';

create table if not exists orders (
      orderid int not null,
      userid varchar(80) not null,
      orderdate date not null,
      shipaddr1 varchar(80) not null,
      shipaddr2 varchar(80) null,
      shipcity varchar(80) not null,
      shipstate varchar(80) not null,
      shipzip varchar(20) not null,
      shipcountry varchar(20) not null,
      billaddr1 varchar(80) not null,
      billaddr2 varchar(80)  null,
      billcity varchar(80) not null,
      billstate varchar(80) not null,
      billzip varchar(20) not null,
      billcountry varchar(20) not null,
      courier varchar(80) not null,
      totalprice decimal(10,2) not null,
      billtofirstname varchar(80) not null,
      billtolastname varchar(80) not null,
      shiptofirstname varchar(80) not null,
      shiptolastname varchar(80) not null,
      creditcard varchar(80) not null,
      exprdate varchar(7) not null,
      cardtype varchar(80) not null,
      locale varchar(80) not null,
primary key (orderid) )
type=innodb 
pack_keys=default 
row_format=default 
comment='cadastro de pedidos';

create table if not exists orderstatus (
      orderid int not null,
      linenum int not null,
      timestamp date not null,
      status varchar(2) not null,
primary key (orderid, linenum) )
type=innodb 
pack_keys=default 
row_format=default 
comment='status de pedidos';

create table if not exists lineitem (
      orderid int not null,
      linenum int not null,
      itemid varchar(10) not null,
      quantity int not null,
      unitprice decimal(10,2) not null,
primary key (orderid, linenum) )
type=innodb 
pack_keys=default 
row_format=default 
comment='line item';

create table if not exists category (
	catid varchar(10) not null,
	name varchar(80) null,
	descn varchar(255) null,
primary key (catid) )
type=innodb 
pack_keys=default 
row_format=default 
comment='categorias';

create table if not exists product (
    productid varchar(10) not null,
    category varchar(10) not null,
    name varchar(80) null,
    descn varchar(255) null,
primary key (productid) )
type=innodb 
pack_keys=default 
row_format=default 
comment='categorias';

alter table product 
	add index productcat(category);

alter table product 
	add index productname(name);

alter table category 
	add index ixcategoryproduct(catid);

alter table product  add foreign key (category) 
         references category(catid) 
         on delete restrict 
         on update restrict;

create table if not exists item (
    itemid varchar(10) not null,
    productid varchar(10) not null,
    listprice decimal(10,2) null,
    unitcost decimal(10,2) null,
    supplier int null,
    status varchar(2) null,
    attr1 varchar(80) null,
    attr2 varchar(80) null,
    attr3 varchar(80) null,
    attr4 varchar(80) null,
    attr5 varchar(80) null,
primary key (itemid) )
type=innodb 
pack_keys=default 
row_format=default 
comment='itens';

alter table item 
	add index itemprod(productid);

alter table item add foreign key (productid) 
         references product(productid) 
         on delete restrict 
         on update restrict;

alter table item add foreign key (supplier) 
         references supplier(suppid) 
         on delete restrict 
         on update restrict;

create table if not exists inventory (
    itemid varchar(10) not null,
    qty int not null,
primary key (itemid) )
type=innodb 
pack_keys=default 
row_format=default 
comment='inventory';

create table if not exists sequence (
    name               varchar(30)  not null,
    nextid             int          not null,
primary key (name) )
type=innodb 
pack_keys=default 
row_format=default 
comment='inventory';
