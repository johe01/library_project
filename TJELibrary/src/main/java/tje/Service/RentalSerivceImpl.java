package tje.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tje.DAO.BookStockDAO;
import tje.DAO.RentalListDAO;
import tje.DAO.UserDAO;
import tje.DTO.BookStock;
import tje.DTO.RentalList;
import tje.DTO.User;

public class RentalSerivceImpl implements RentalService {

	UserDAO userDAO  = new UserDAO();
	RentalListDAO rentalListDAO = new RentalListDAO();
	BookStockDAO bookStockDAO = new BookStockDAO();
	
	@Override
	public RentalList select(User user) {
		
		RentalList rentalList = null;
		
		// 회원ID로 대출내역 조회
		try {
			Map<Object, Object> fields = new HashMap<Object, Object>() {{
				put("id", user.getId());
			}};
			rentalList = rentalListDAO.selectBy(fields);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rentalList;
	}

	@Override
	public int Reservation(BookStock bookStock, User user) {
	    int result = 0;  // 결과값(성공 여부) 반환용

	    try {
	        if (bookStock == null) {
	            System.err.println("bookStock이 null입니다. 유효한 도서를 선택해 주세요.");
	            return 0; // 실패 처리
	        }
	    	
	        // 1. 도서가 '대출 가능'한 상태인지 확인
	        if ("대출 가능".equals(bookStock.getStatus())) {
	            // 2. 도서 상태를 '예약 중'으로 변경
	            bookStock.setStatus("예약 중");
	            bookStockDAO.update(bookStock);  // 변경된 도서 상태 DB에 저장

	            // 3. 대출 내역 등록
	            RentalList rental = new RentalList(bookStock.getStockId(), bookStock.getBookId(), user.getId() );
	            rental.setState("예약");
//	            LocalDate localDate = LocalDate.now();
//	    		Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//	    		Date date = Date.from(instant);
//	    		rental.setRentalDate(date);
	    		
	            rentalListDAO.insert(rental);  // 대출 내역 DB에 등록

	            System.out.println("예약이 완료되었습니다.");
	            result = 1;  // 성공 시 1 반환
	        } else {
	            System.err.println("이미 예약된 도서이거나 대출 불가능한 도서입니다.");
	        }
	    } catch (Exception e) {
	        System.err.println("예약 처리 중 오류가 발생했습니다: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return result;  // 성공(1) 또는 실패(0) 반환
	}

	@Override
	public int rvDelete(BookStock bookStock, User user) {
	    int result = 0; // 결과값(성공 여부) 반환용

	    try {
	        // 1. 도서가 '예약 중'인지 확인
	        if ("예약 중".equals(bookStock.getStatus())) {
	        	Map<Object, Object> rlfield = new HashMap<Object, Object>() {{
	            	put("id", user.getId());
	                put("stock_id", bookStock.getStockId());
	                put("book_id", bookStock.getBookId());
	                put("state", "예약");
	            }};
	            // 2. 도서 상태를 '대출 가능'으로 변경
	            bookStock.setStatus("대출 가능");
	        	
	            bookStockDAO.update(bookStock);  // 변경된 도서 상태 DB에 저장

	            // 3. 대출 내역 삭제
	            result = rentalListDAO.deleteBy(rlfield);

	            if (result > 0) {
	                System.out.println("예약이 취소되었습니다.");
	                result = 1; // 성공 시 1 반환
	            } else {
	                System.err.println("예약 취소에 실패했습니다.");
	            }
	        } else {
	            System.err.println("예약된 도서가 아닙니다.");
	        }
	    } catch (Exception e) {
	        System.err.println("예약 취소 중 오류가 발생했습니다: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return result;  // 성공(1) 또는 실패(0) 반환
	}

	@Override
	public long overdue(BookStock bookStock, User user) {
		
		// 연체 (반납 예정일)
		
		RentalList rentalList = new RentalList();
		Map<Object, Object> fields = new HashMap<Object, Object>() {{
		put("book_id", bookStock.getBookId());
		put("stock_id", bookStock.getStockId());
		put("id", user.getId());
		put("state", "대출");
		}};
		try {
			rentalList = rentalListDAO.selectBy(fields);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Calendar overday = Calendar.getInstance();  // 오늘 날짜
		overday.setTime(rentalList.getRentalDate()); // RentalList에서 반납 예정일 가져오기
		overday.add(Calendar.DAY_OF_MONTH, 7);

	    Calendar today = Calendar.getInstance();  // 오늘 날짜

	    long dM = today.getTimeInMillis() - overday.getTimeInMillis(); // 밀리초 차이
	    long dD = dM / (24 * 60 * 60 * 1000); // 일수 차이 계산

	    System.out.println("차이(일) : " + dD);

	    // 반납 예정일이 지났다면, dD를 반환하거나 다른 처리 로직을 추가할 수 있습니다.
	    return (dD > 0) ? dD : 0; // 반납 예정일이 지났다면 dD 반환, 그렇지 않으면 0
	}
	
	@Override
	public int rental(BookStock bookStock, User user) {
		// 대출
		
		BookStock dbBookStock = null;
		RentalList dbRentalList = null;
		
		Map<Object, Object> fields = new HashMap<Object, Object>() {{
            put("stock_id", bookStock.getStockId());
            put("book_id", bookStock.getBookId());
        }};
        
        Map<Object, Object> fields2 = new HashMap<Object, Object>() {{
        	put("id", user.getId());
            put("stock_id", bookStock.getStockId());
            put("book_id", bookStock.getBookId());
            put("state", "예약");
        }};
        
		try {
			dbBookStock = bookStockDAO.selectBy(fields);
		} catch (Exception e) {
			System.err.println("대출 중 bookStockDAO.selectBy()에서 에러 발생");
			e.printStackTrace();
		}
		if (dbBookStock.getStatus().equals("대출 불가")) {
			return 0;
		} else if(dbBookStock.getStatus().equals("예약 중")){
			try {
				dbRentalList = rentalListDAO.selectBy(fields2);
			} catch (Exception e) {
				System.err.println("대출 중 rentalListDAO.selectBy()");
				e.printStackTrace();
			}
			if(dbRentalList==null) return 0;
			try {
				rentalListDAO.deleteBy(fields2);
			} catch (Exception e) {
				System.err.println("대출 중 rentalListDAO.deleteBy(fields2)");
				e.printStackTrace();
			}
		}
		
		RentalList rsRentalList = new RentalList();
		rsRentalList.setId(user.getId());
		rsRentalList.setBookId(bookStock.getBookId());
		rsRentalList.setStockId(bookStock.getStockId());
		rsRentalList.setState("대출");
		LocalDate localDate = LocalDate.now();
		Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
		Date date = Date.from(instant);
		rsRentalList.setRentalDate(date);
		
		try {
			rentalListDAO.insert(rsRentalList);
		} catch (Exception e) {
			System.err.println("대출 등록 실패!");
			e.printStackTrace();
		}
		
		BookStock rsBookStock = new BookStock();
		rsBookStock.setBookId(bookStock.getBookId());
		rsBookStock.setStockId(bookStock.getStockId());
		rsBookStock.setStatus("대출 불가");
		try {
			bookStockDAO.update(rsBookStock);
		} catch (Exception e) {
			System.err.println("스테이터스 변경 실패!");
			e.printStackTrace();
		}
		return 1;
	}

	@Override
	public int returned(RentalList rentalList) {
		// 반납 등록
		int result = 0;

		LocalDate localDate = LocalDate.now();
		Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
		Date date = Date.from(instant);
		rentalList.setReturnDate(date);
		
		BookStock bookStock = new BookStock();
		bookStock.setBookId(rentalList.getBookId());
		bookStock.setStockId(rentalList.getStockId());
		
		User user = new User();
		user.setId(rentalList.getId());
		
		try {
			rentalListDAO.update(rentalList);
		} catch (Exception e) {
			System.err.println("반납 중 rentalListDAO.update(rentalList, \"return_date\")");
			e.printStackTrace();
		}
		
		// 대출 상태
		int statusResult = 0;
		bookStock.setStatus("대출 가능");
		try {
			statusResult = bookStockDAO.update(bookStock);
			if ( statusResult > 0 ) System.out.println("스테이터스 등록 성공!");
		} catch (Exception e) {
			System.err.println("스테이터스 변경 실패!");
			e.printStackTrace();
		}
		if ( statusResult == 0 ) return 0;
		
		long a = overdue(bookStock, user);
		
		if ( a > 0 ) {
			rentalList.setState("연체");
			rentalList.setOverDate((int)a);
			try {
				result = rentalListDAO.update(rentalList);
				if ( result > 0 ) System.out.println("반납 등록 성공!");
			} catch (Exception e) {
				System.err.println("반납 등록 실패!");
				e.printStackTrace();
			}
			if ( result == 0 ) return 0;
		}
		else {
			rentalList.setState("반납");
			try {
				rentalListDAO.update(rentalList);
			} catch (Exception e) {
				System.err.println("반납 중 rentalListDAO.update(rentalList, \"state\");");
				e.printStackTrace();
			}
		}
		
		return statusResult;
	}

	@Override
	public List<RentalList> selectlist(User user) {
		List<RentalList> rentalListlist = null;
		RentalListDAO rentalListDAO = new RentalListDAO();
		Map<Object, Object> fields = new HashMap<Object, Object>() {{
            put("id", user.getId());
        }};
        try {
			rentalListlist = rentalListDAO.listBy(fields);
		} catch (Exception e) {
			System.out.println("대출내역목록조회 중 오류 발생");
			e.printStackTrace();
		}
        if (rentalListlist == null) return null;
		return rentalListlist;
	}

	@Override
	public List<RentalList> selectByState(String state) {
		List<RentalList> stList = new ArrayList<RentalList>();
		Map<Object, Object> fields = new HashMap<Object, Object>() {{
            put("state", state);
        }};
        try {
			stList = rentalListDAO.listBy(fields);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stList;
	}

}
