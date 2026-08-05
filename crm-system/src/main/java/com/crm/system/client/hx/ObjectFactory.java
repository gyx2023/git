package com.crm.system.client.hx;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the com.sdunicomsi.oppo.service package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _GetRemandStatus_QNAME = new QName("http://service.oppo.sdunicomsi.com/",
			"getRemandStatus");
	private final static QName _GetRemandStatusResponse_QNAME = new QName("http://service.oppo.sdunicomsi.com/",
			"getRemandStatusResponse");
	private final static QName _RemandContractSaveResponse_QNAME = new QName("http://service.oppo.sdunicomsi.com/",
			"remandContractSaveResponse");
	private final static QName _PushInfoToOppoSys_QNAME = new QName("http://service.oppo.sdunicomsi.com/",
			"pushInfoToOppoSys");
	private final static QName _RemandContractSave_QNAME = new QName("http://service.oppo.sdunicomsi.com/",
			"remandContractSave");
	private final static QName _PushInfoToOppoSysResponse_QNAME = new QName("http://service.oppo.sdunicomsi.com/",
			"pushInfoToOppoSysResponse");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: com.sdunicomsi.oppo.service
	 *
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link RemandContractBean }
	 *
	 */
	public RemandContractBean createRemandContractBean() {
		return new RemandContractBean();
	}

	/**
	 * Create an instance of {@link RemandStatusSelectBean }
	 *
	 */
	public RemandStatusSelectBean createRemandStatusSelectBean() {
		return new RemandStatusSelectBean();
	}

	/**
	 * Create an instance of {@link PushInfoToOppoSysResponse }
	 *
	 */
	public PushInfoToOppoSysResponse createPushInfoToOppoSysResponse() {
		return new PushInfoToOppoSysResponse();
	}

	/**
	 * Create an instance of {@link PushInfoToOppoSys }
	 *
	 */
	public PushInfoToOppoSys createPushInfoToOppoSys() {
		return new PushInfoToOppoSys();
	}

	/**
	 * Create an instance of {@link RemandContractSave }
	 *
	 */
	public RemandContractSave createRemandContractSave() {
		return new RemandContractSave();
	}

	/**
	 * Create an instance of {@link RemandContractSaveResponse }
	 *
	 */
	public RemandContractSaveResponse createRemandContractSaveResponse() {
		return new RemandContractSaveResponse();
	}

	/**
	 * Create an instance of {@link GetRemandStatus }
	 *
	 */
	public GetRemandStatus createGetRemandStatus() {
		return new GetRemandStatus();
	}

	/**
	 * Create an instance of {@link GetRemandStatusResponse }
	 *
	 */
	public GetRemandStatusResponse createGetRemandStatusResponse() {
		return new GetRemandStatusResponse();
	}

	/**
	 * Create an instance of {@link ContractBean }
	 *
	 */
	public ContractBean createContractBean() {
		return new ContractBean();
	}

	/**
	 * Create an instance of {@link MsgHeader }
	 *
	 */
	public MsgHeader createMsgHeader() {
		return new MsgHeader();
	}

	/**
	 * Create an instance of {@link RemandStatusBean }
	 *
	 */
	public RemandStatusBean createRemandStatusBean() {
		return new RemandStatusBean();
	}

	/**
	 * Create an instance of {@link RemandCodeBean }
	 *
	 */
	public RemandCodeBean createRemandCodeBean() {
		return new RemandCodeBean();
	}

	/**
	 * Create an instance of {@link OutParam }
	 *
	 */
	public OutParam createOutParam() {
		return new OutParam();
	}

	/**
	 * Create an instance of {@link RemandContractBean.ContractList }
	 *
	 */
	public RemandContractBean.ContractList createRemandContractBeanContractList() {
		return new RemandContractBean.ContractList();
	}

	/**
	 * Create an instance of {@link RemandStatusSelectBean.RemandCodeList }
	 *
	 */
	public RemandStatusSelectBean.RemandCodeList createRemandStatusSelectBeanRemandCodeList() {
		return new RemandStatusSelectBean.RemandCodeList();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link GetRemandStatus }
	 * {@code >}
	 *
	 */
	@XmlElementDecl(namespace = "http://service.oppo.sdunicomsi.com/", name = "getRemandStatus")
	public JAXBElement<GetRemandStatus> createGetRemandStatus(GetRemandStatus value) {
		return new JAXBElement<GetRemandStatus>(_GetRemandStatus_QNAME, GetRemandStatus.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link GetRemandStatusResponse }{@code >}
	 *
	 */
	@XmlElementDecl(namespace = "http://service.oppo.sdunicomsi.com/", name = "getRemandStatusResponse")
	public JAXBElement<GetRemandStatusResponse> createGetRemandStatusResponse(GetRemandStatusResponse value) {
		return new JAXBElement<GetRemandStatusResponse>(_GetRemandStatusResponse_QNAME, GetRemandStatusResponse.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link RemandContractSaveResponse }{@code >}
	 *
	 */
	@XmlElementDecl(namespace = "http://service.oppo.sdunicomsi.com/", name = "remandContractSaveResponse")
	public JAXBElement<RemandContractSaveResponse> createRemandContractSaveResponse(RemandContractSaveResponse value) {
		return new JAXBElement<RemandContractSaveResponse>(_RemandContractSaveResponse_QNAME,
				RemandContractSaveResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link PushInfoToOppoSys }
	 * {@code >}
	 *
	 */
	@XmlElementDecl(namespace = "http://service.oppo.sdunicomsi.com/", name = "pushInfoToOppoSys")
	public JAXBElement<PushInfoToOppoSys> createPushInfoToOppoSys(PushInfoToOppoSys value) {
		return new JAXBElement<PushInfoToOppoSys>(_PushInfoToOppoSys_QNAME, PushInfoToOppoSys.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link RemandContractSave }{@code >}
	 *
	 */
	@XmlElementDecl(namespace = "http://service.oppo.sdunicomsi.com/", name = "remandContractSave")
	public JAXBElement<RemandContractSave> createRemandContractSave(RemandContractSave value) {
		return new JAXBElement<RemandContractSave>(_RemandContractSave_QNAME, RemandContractSave.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link PushInfoToOppoSysResponse }{@code >}
	 *
	 */
	@XmlElementDecl(namespace = "http://service.oppo.sdunicomsi.com/", name = "pushInfoToOppoSysResponse")
	public JAXBElement<PushInfoToOppoSysResponse> createPushInfoToOppoSysResponse(PushInfoToOppoSysResponse value) {
		return new JAXBElement<PushInfoToOppoSysResponse>(_PushInfoToOppoSysResponse_QNAME,
				PushInfoToOppoSysResponse.class, null, value);
	}

}
