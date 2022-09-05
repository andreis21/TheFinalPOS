package com.pos.servlet;

import com.pos.bean.CategoryBean;
import com.pos.bean.ProductBean;
import com.pos.bean.RoleBean;
import com.pos.bean.TransactionTypeBean;
import com.pos.bean.UserBean;
import com.pos.entity.Category;
import com.pos.entity.Product;
import com.pos.entity.TransactionType;
import com.pos.entity.UserTable;
import com.pos.utility.CurrentCarts;
import com.pos.utility.LoggedUsers;
import com.pos.utility.Notification;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "View", urlPatterns = {"/View"})
public class View extends HttpServlet {

    @Inject
    UserBean userBean;

    @Inject
    RoleBean roleBean;
    
    @Inject
    ProductBean productBean;
    
    @Inject
    CategoryBean categoryBean;

    @Inject
    TransactionTypeBean transactionTypeBean;

    UserTable loggedUser = null;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = Integer.parseInt(request.getParameter("userId"));

        try {
            loggedUser = userBean.getById(userId);
        }catch(Exception ex){
            response.sendRedirect("http://localhost:8080/TheFinalPOS/");
        }
        
        if (LoggedUsers.getInstance().isUserLogged(loggedUser)) {
            redirectRole(loggedUser, request, response);
        }else {
            response.sendRedirect("http://localhost:8080/TheFinalPOS/");
        }

    }

    private void redirectRole(UserTable user, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("loggedUser", user);

        if (user.getIdRole().equals(roleBean.findByName("Cashier"))) {
            List<TransactionType> types = transactionTypeBean.getAllCategories();
            request.setAttribute("types", types);
            
            if (CurrentCarts.getInstance().doesCashierHaveCart(user.getId())) {
                CurrentCarts.getInstance().getCartByCashierId(user.getId()).getCartState().initializeCart(user.getId());
            }
            
            request.getRequestDispatcher("/WEB-INF/pages/cashierView.jsp").forward(request, response);
        } else if (user.getIdRole().equals(roleBean.findByName("Director"))) {
            Notification.events.attach(Notification.listener);
            Notification.decoratorEvents.attach(Notification.listener);

            List<UserTable> users = userBean.getAllUsers();
            List<Category> allCategories = categoryBean.getAllCategories();
            List<Product> allProducts = productBean.getAllProducts();

            request.setAttribute("allUsers", users);
            request.setAttribute("notificationCount", Notification.notificationCount);
            request.setAttribute("notificationMessage", Notification.listener);
            request.setAttribute("allCategories", allCategories);
            request.setAttribute("allProducts", allProducts);
            
            request.getRequestDispatcher("/WEB-INF/pages/directorView.jsp").forward(request, response);
        } else if (user.getIdRole().equals(roleBean.findByName("Admin"))) {
            List<UserTable> users = userBean.getAllUsers();
            List<Product> allProducts = productBean.getAllProducts();
            List<Category> allCategories = categoryBean.getAllCategories();
            
            request.setAttribute("allUsers", users);
            request.setAttribute("allProducts", allProducts);
            request.setAttribute("allCategories", allCategories);
            
            request.getRequestDispatcher("/WEB-INF/pages/adminView.jsp").forward(request, response);
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
