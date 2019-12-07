/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package ch.cordalo.corda.common.contracts;

public class ExamplePermissions extends Permissions {

    private static final ExamplePermissions INSTANCE = new ExamplePermissions();

    public static ExamplePermissions getInstance() {
        return INSTANCE;
    }

    public ExamplePermissions() {
        super("example");
    }

    /*
        newTransition("CREATE", "CREATED");

        newTransition("REGISTER", "REGISTERED", "CREATED");
        newTransition("INFORM", "INFORMED", "CREATED", "REGISTERED");
        newTransition("CONFIRM", "CONFIRMED", "INFORMED");
        newTransition("TIMEOUT", "TIMEOUTS", "INFORMED");

        newTransition("UPDATE", "", "CREATED", "SHARED");

        newTransition("WITHDRAW", "WITHDRAWN", "CONFIRMED", "INFORMED", "REGISTERED", "CREATED");
        newTransition("NO_SHARE", "NOT_SHARED", "CONFIRMED", "INFORMED");
        newTransition("DUPLICATE", "DUPLICATE", "CONFIRMED", "INFORMED", "REGISTERED", "CREATED");
        newTransition("SHARE", "SHARED", "CONFIRMED", "INFORMED", "REGISTERED", "CREATED");

        newTransition("SEND_PAYMENT", "PAYMENT_SENT", "SHARED");

        newTransition("DECLINE", "DECLINED", "SHARED", "PAYMENT_SENT");
        newTransition("ACCEPT", "ACCEPTED", "SHARED", "PAYMENT_SENT");

     */
    @Override
    protected void initPermissions() {
        this.addStateActionsForRole("admin",
                "CREATE",
                "REGISTER",
                "INFORM",
                "CONFIRM",
                "TIMEOUT",
                "WITHDRAW",
                "NO_SHARE",
                "DUPLICATE",
                "SHARE",
                "SEND_PAYMENT",
                "DECLINE",
                "ACCEPT"
        );
        this.addCommandActionaForRole("searcher", "search");
        this.addStateActionsForRole("creator", "CREATE", "REGISTER");
        this.addStateActionsForRole("decider", "DECLINE", "ACCEPT");
    }

    @Override
    protected void initPartiesAndRoles() {
        this.addPartyAndRoles("CN=Company-A,OU=IT,O=organisation,L=locality,ST=state,C=CH", "admin", "decider");
        this.addPartyAndRoles("CN=Company-A,OU=Marketing,O=organisation,L=locality,ST=state,C=CH", "searcher");
        this.addPartyAndRoles("CN=Company-B,OU=IT,O=organisation,L=locality,ST=state,C=CH", "searcher", "creator");
        this.addPartyAndRoles("CN=Company-B,OU=Business,O=organisation,L=locality,ST=state,C=CH", "decider");
        this.addPartyAndRoles("CN=Company-C,OU=IT,O=organisation,L=locality,ST=state,C=CH", "none");
    }

    @Override
    protected void initPartiesAndAttributes() {
        this.addPartyAndAttribute("CN=Company-A,OU=IT,O=organisation,L=locality,ST=state,C=CH", "logo", "companyA.png");
        this.addPartyAndAttribute("CN=Company-A,OU=IT,O=organisation,L=locality,ST=state,C=CH", "products", "motor,household");
    }
}
