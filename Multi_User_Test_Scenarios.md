# Multi-User Finance Tracking System - Test Scenarios

## Overview
This document outlines comprehensive test scenarios to validate the complete data isolation between different users and proper session switching functionality in the Finance Tracking app.

## Test Environment Setup
1. Clean app installation on test device
2. No existing user data
3. Database is empty

## Test Group 1: User Registration and Login

### Test Case 1.1: First User Registration
**Objective**: Verify first-time user registration works correctly

**Steps**:
1. Launch the app
2. UserSelectionActivity should appear with create user dialog
3. Fill in user details:
   - Username: testuser1
   - Email: test1@example.com
   - Password: password123
   - First Name: Test
   - Last Name: User One
4. Tap "Create"

**Expected Results**:
- User created successfully
- Auto-login to MainActivity
- Dashboard shows empty state (no transactions)
- Settings show correct user info

### Test Case 1.2: Second User Registration
**Objective**: Verify additional user registration

**Steps**:
1. Navigate to Settings
2. Tap "Switch User"
3. Tap "Add New User"
4. Create second user:
   - Username: testuser2
   - Email: test2@example.com
   - Password: password456
   - First Name: Test
   - Last Name: User Two

**Expected Results**:
- Second user created successfully
- Auto-login as second user
- Dashboard shows empty state (no data from user 1)
- Settings show second user info

## Test Group 2: Data Isolation Testing

### Test Case 2.1: Create Transactions for User 1
**Steps**:
1. Switch to testuser1 (if not already logged in)
2. Add income transaction:
   - Amount: ₹5000
   - Category: Salary
   - Description: Monthly salary
   - Date: Current date
3. Add expense transaction:
   - Amount: ₹200
   - Category: Food & Dining
   - Description: Grocery shopping
   - Date: Current date

**Expected Results**:
- Transactions saved successfully
- Dashboard shows correct balance: $4800
- Recent transactions list shows both entries

### Test Case 2.2: Create Transactions for User 2
**Steps**:
1. Switch to testuser2
2. Add income transaction:
   - Amount: ₹3000
   - Category: Business
   - Description: Freelance payment
   - Date: Current date
3. Add expense transaction:
   - Amount: ₹
150
   - Category: Transportation
   - Description: Gas and maintenance
   - Date: Current date

**Expected Results**:
- Transactions saved successfully
- Dashboard shows correct balance: $2850
- Recent transactions list shows only User 2's entries
- No data from User 1 visible

### Test Case 2.3: Verify Data Isolation
**Steps**:
1. Switch between testuser1 and testuser2 multiple times
2. Check dashboard data for each user
3. Check transaction lists
4. Check balance calculations

**Expected Results**:
- User 1: Balance $4800, 2 transactions
- User 2: Balance $2850, 2 transactions
- No cross-contamination of data
- Each user sees only their own data

## Test Group 3: Session Management

### Test Case 3.1: Session Persistence
**Steps**:
1. Login as testuser1
2. Close app completely (force stop)
3. Reopen app after 5 minutes

**Expected Results**:
- App opens directly to MainActivity (no re-login required)
- User 1's data is displayed
- Session is maintained

### Test Case 3.2: Session Timeout
**Steps**:
1. Login as testuser1
2. Leave app inactive for more than 24 hours (or modify timeout for testing)
3. Reopen app

**Expected Results**:
- App redirects to UserSelectionActivity
- User must re-authenticate
- After re-login, all data is intact

### Test Case 3.3: User Switching
**Steps**:
1. Login as testuser1
2. Navigate to Settings → Switch User
3. Select testuser2 from list
4. Enter password for testuser2

**Expected Results**:
- Successfully switches to User 2
- App restarts with User 2's data
- All User 2's transactions and balance displayed
- Settings show User 2's profile information

## Test Group 4: User Management

### Test Case 4.1: Profile Editing
**Steps**:
1. Login as testuser1
2. Navigate to Settings → Edit Profile
3. Change First Name to "Updated"
4. Change Email to "updated@example.com"
5. Save changes

**Expected Results**:
- Profile updated successfully
- Settings display shows new information
- Username remains unchanged (read-only)

### Test Case 4.2: Password Change
**Steps**:
1. Login as testuser1
2. Navigate to Settings → Manage Users → testuser1 → Change Password
3. Enter current password: password123
4. Enter new password: newpassword123
5. Confirm new password

**Expected Results**:
- Password changed successfully
- Next login requires new password
- Old password no longer works

### Test Case 4.3: User Deletion
**Steps**:
1. Login as testuser1
2. Navigate to Settings → Manage Users
3. Select testuser2 → Delete User
4. Confirm deletion

**Expected Results**:
- User 2 deleted successfully
- User 2's data completely removed
- User 2 no longer appears in user list
- Cannot login as User 2 anymore

## Test Group 5: Data Integrity

### Test Case 5.1: Budget Creation Isolation
**Steps**:
1. Login as testuser1
2. Create monthly budget: Food & Dining - $500
3. Switch to testuser2
4. Check budgets section

**Expected Results**:
- User 1's budget saved correctly
- User 2 sees no budgets (empty state)
- Budget data is user-specific

### Test Case 5.2: Category Isolation
**Steps**:
1. Login as testuser1
2. Create custom category: "Investments"
3. Add transaction with this category
4. Switch to testuser2
5. Check available categories

**Expected Results**:
- User 1 can see and use "Investments" category
- User 2 sees only default categories
- Custom category is user-specific

### Test Case 5.3: Analytics Isolation
**Steps**:
1. Ensure both users have different transaction data
2. Login as testuser1
3. Check Reports/Analytics section
4. Note monthly summaries and category breakdowns
5. Switch to testuser2
6. Check Reports/Analytics section

**Expected Results**:
- Each user sees only their own analytics
- Monthly summaries are user-specific
- Category breakdowns show only user's data
- No data mixing between users

## Test Group 6: Error Handling

### Test Case 6.1: Duplicate Username Prevention
**Steps**:
1. Try to create new user with username "testuser1" (already exists)

**Expected Results**:
- Error message displayed
- User creation prevented
- Existing user data unaffected

### Test Case 6.2: Invalid Password Recovery
**Steps**:
1. Try to login as testuser1 with wrong password
2. Attempt multiple times

**Expected Results**:
- Login denied
- Error message shown
- User remains on login screen
- No access to user data

## Test Group 7: Data Migration

### Test Case 7.1: Upgrade from Single to Multi-User
**Prerequisites**: Have app with single user data installed

**Steps**:
1. Update to multi-user version
2. Launch app

**Expected Results**:
- Existing data preserved
- App functions normally
- User can create additional accounts

## Success Criteria

### Core Functionality
- ✅ Multiple users can create accounts
- ✅ Each user has completely isolated data
- ✅ User switching works seamlessly
- ✅ Session management functions correctly
- ✅ All financial data (transactions, budgets, categories) is user-specific

### Security & Privacy
- ✅ Password authentication required for each user
- ✅ No data leakage between users
- ✅ Session timeout for security
- ✅ Safe user deletion with data cleanup

### User Experience
- ✅ Intuitive user selection interface
- ✅ Smooth user switching
- ✅ Profile management capabilities
- ✅ Clear user identification in UI

## Test Execution Checklist

### Pre-Testing
- [ ] Fresh app installation
- [ ] Test device ready
- [ ] Test data prepared

### User Registration
- [ ] Test Case 1.1: First user registration
- [ ] Test Case 1.2: Second user registration

### Data Isolation
- [ ] Test Case 2.1: User 1 transactions
- [ ] Test Case 2.2: User 2 transactions  
- [ ] Test Case 2.3: Data isolation verification

### Session Management
- [ ] Test Case 3.1: Session persistence
- [ ] Test Case 3.2: Session timeout
- [ ] Test Case 3.3: User switching

### User Management
- [ ] Test Case 4.1: Profile editing
- [ ] Test Case 4.2: Password change
- [ ] Test Case 4.3: User deletion

### Data Integrity
- [ ] Test Case 5.1: Budget isolation
- [ ] Test Case 5.2: Category isolation
- [ ] Test Case 5.3: Analytics isolation

### Error Handling
- [ ] Test Case 6.1: Duplicate username
- [ ] Test Case 6.2: Invalid password

### Final Validation
- [ ] All test cases passed
- [ ] No data corruption observed
- [ ] Performance acceptable
- [ ] User experience smooth

## Bug Report Template

**Test Case**: [Test Case Number and Name]
**Expected Result**: [What should happen]
**Actual Result**: [What actually happened]
**Steps to Reproduce**: [Detailed steps]
**Severity**: [High/Medium/Low]
**Screenshots**: [If applicable]

## Notes
- Test with both new installations and upgrades
- Test on different Android versions if possible
- Verify memory usage with multiple users
- Check database integrity after all tests
- Document any edge cases discovered during testing