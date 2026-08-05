package com.crm.system.client.hx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * pushInfoToOppoSys complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="pushInfoToOppoSys">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="msgHeader" type="{http://service.oppo.sdunicomsi.com/}msgHeader" minOccurs="0"/>
 *         &lt;element name="remandId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pushInfoToOppoSys", propOrder = { "msgHeader", "remandId" })
public class PushInfoToOppoSys {

	protected MsgHeader msgHeader;
	protected String remandId;

	/**
	 * ��ȡmsgHeader���Ե�ֵ��
	 *
	 * @return possible object is {@link MsgHeader }
	 *
	 */
	public MsgHeader getMsgHeader() {
		return msgHeader;
	}

	/**
	 * ����msgHeader���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link MsgHeader }
	 *
	 */
	public void setMsgHeader(MsgHeader value) {
		this.msgHeader = value;
	}

	/**
	 * ��ȡremandId���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getRemandId() {
		return remandId;
	}

	/**
	 * ����remandId���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setRemandId(String value) {
		this.remandId = value;
	}

}
