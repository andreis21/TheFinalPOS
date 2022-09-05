package com.pos.servlet;

import com.pos.bean.TransactionBean;
import com.pos.bean.UserBean;
import com.pos.entity.TransactionTable;
import com.pos.utility.ParseDateTimeValue;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Reports", urlPatterns = {"/Reports"})
public class Reports extends HttpServlet {

    @Inject
    TransactionBean transactionBean;
    
    @Inject
    UserBean userBean;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String report = null;
        report = request.getParameter("report");
        int loggedId = Integer.parseInt(request.getParameter("loggedUserId"));
        request.setAttribute("loggedUser", userBean.getById(loggedId));

        if (report != null) {
            if (report.equals("all")) {
                List<TransactionTable> transactions = transactionBean.getAllTransactions();
                request.setAttribute("allTransactions", transactions);
                request.getRequestDispatcher("/WEB-INF/pages/reportTransactionsTable.jsp").forward(request, response);
            } else if (report.equals("values")) {
                int fromValue = Integer.parseInt(request.getParameter("fromValue"));
                int toValue = Integer.parseInt(request.getParameter("toValue"));

                List<TransactionTable> transactions = transactionBean.getTransactionsBetweenValues(fromValue, toValue);
                request.setAttribute("allTransactions", transactions);
                request.getRequestDispatcher("/WEB-INF/pages/reportTransactionsTable.jsp").forward(request, response);
            } else if (report.equals("dates")) {
                String fromDate = ParseDateTimeValue.fromStringDateToTimestamp(request.getParameter("fromDate"));
                String toDate = ParseDateTimeValue.fromStringDateToTimestamp(request.getParameter("toDate"));
                
                System.out.println(fromDate);
                
                List<TransactionTable> transactions = transactionBean.getTransactionsBetweenDates(fromDate, toDate);
                request.setAttribute("allTransactions", transactions);
                request.getRequestDispatcher("/WEB-INF/pages/reportTransactionsTable.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher("/WEB-INF/pages/reports.jsp").forward(request, response);
            }
        } else {
            request.getRequestDispatcher("/WEB-INF/pages/reports.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
