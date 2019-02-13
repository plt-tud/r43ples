package de.tud.plt.r43ples.exception;

import org.apache.jena.rdf.model.Model;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;

public class OutdatedException extends InternalErrorException{

    /** The default serial version UID **/
    private static final long serialVersionUID = 1L;
    private final Model clientModel;
    private final Model serverModel;


    public OutdatedException(Model clientModel, Model serverModel) {
        super(String.format("Client information is not up to date. The following statements have changed: \n" +
                        "Added:\n %s\n" +
                        "Deleted:\n %s\n",
                JenaModelManagement.convertJenaModelToNTriple(serverModel.difference(clientModel)),
                JenaModelManagement.convertJenaModelToNTriple(clientModel.difference(serverModel))));
        this.clientModel = clientModel;
        this.serverModel = serverModel;
        }
}
