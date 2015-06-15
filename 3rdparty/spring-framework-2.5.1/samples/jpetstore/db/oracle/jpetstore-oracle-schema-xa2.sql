drop sequence linenum;
drop sequence ordernum;

drop table lineitem;
drop table orderstatus;
drop table orders;

create table orders (
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
      totalprice number(10,2) not null,
      billtofirstname varchar(80) not null,
      billtolastname varchar(80) not null,
      shiptofirstname varchar(80) not null,
      shiptolastname varchar(80) not null,
      creditcard varchar(80) not null,
      exprdate varchar(7) not null,
      cardtype varchar(80) not null,
      locale varchar(80) not null,
      constraint pk_orders primary key (orderid)
);

grant all on orders to public;
create sequence ordernum increment by 1 cache 10000;

create table orderstatus (
      orderid int not null,
      linenum int not null,
      timestamp date not null,
      status varchar(2) not null,
      constraint pk_orderstatus primary key (orderid, linenum),
      constraint fk_orderstatus_1 foreign key (orderid)
	references orders (orderid)
);

grant all on orderstatus to public;
create sequence linenum increment by 1 cache 10000;

create table lineitem (
      orderid int not null,
      linenum int not null,
      itemid varchar(10) not null,
      quantity int not null,
      unitprice number(10,2) not null,
      constraint pk_lineitem primary key (orderid, linenum),
      constraint fk_lineitem_1 foreign key (orderid)
	references orders (orderid)
);

grant all on lineitem to public;
