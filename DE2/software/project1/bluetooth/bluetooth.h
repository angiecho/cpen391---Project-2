#ifndef BLUETOOTH_H_
#define BLUETOOTH_H_

#define Bluetooth_Status 		(*(volatile unsigned char *)(0x84000220))
#define Bluetooth_Control 		(*(volatile unsigned char *)(0x84000220))
#define Bluetooth_TxData 		(*(volatile unsigned char *)(0x84000222))
#define Bluetooth_RxData 		(*(volatile unsigned char *)(0x84000222))

#define Bluetooth_Status2 		(*(volatile unsigned char *)(0x84000226))
#define Bluetooth_Control2 		(*(volatile unsigned char *)(0x84000226))
#define Bluetooth_TxData2 		(*(volatile unsigned char *)(0x84000228))
#define Bluetooth_RxData2 		(*(volatile unsigned char *)(0x84000228))

#define Bluetooth_Baud    		(*(volatile unsigned char *)(0x84000224))

/**************************************************************************
***
**  Initialise bluetooth
*****************************************************************************/
void putCharBluetooth(char c);
char getCharBluetooth();
void Init_Bluetooth(void);
void WaitForReadStat();

void putCharBluetooth2(char c);
char getCharBluetooth2();

/*****************************************************************************
**   Switch modes
*****************************************************************************/
void commandMode(void);
void dataMode(void);
void slaveMode(void);
void endCommand();
void setName();

/*****************************************************************************
**   Set name/security pin
*****************************************************************************/
void changeName(char name[]);
void changePassword(char pw[]);
void assignBluetooth(char* name, char* pw);
void sendTestData(char* word);

#endif /* BLUETOOTH_H_ */
