import React, { useState } from "react";
import { useRouter } from "expo-router";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Image,
  Alert,
} from "react-native";
import axios from "axios";

const arrowLeft = require("../../assets/images/arrow_left.png");

const BASE_URL = "http://172.18.38.26:8080";

export default function PasswordChangeScreen() {
  const router = useRouter();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const adminId = 1;

  const onBack = () => {
    router.back();
  };

  const handleChangePassword = async () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      Alert.alert("오류", "모든 입력란을 채워주세요.");
      return;
    }
    if (newPassword !== confirmPassword) {
      Alert.alert("오류", "새 비밀번호가 일치하지 않습니다.");
      return;
    }

    try {
      const response = await axios.post(
        `${BASE_URL}/api/admin/change-password`,
        {
          adminId,
          currentPassword,
          newPassword,
        }
      );

      Alert.alert("성공", "비밀번호가 변경되었습니다.");
      router.back();
    } catch (error) {
      console.error("❌ 비밀번호 변경 실패:", error);
      Alert.alert("오류", "비밀번호 변경에 실패했습니다.");
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity
          style={{ width: 40, alignItems: "flex-start" }}
          onPress={onBack}
        >
          <Image source={arrowLeft} style={styles.arrowIcon} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>비밀번호 변경</Text>
        <View style={{ width: 40 }} />
      </View>

      <View style={styles.contentBox}>
        <View style={{ marginTop: 15 }}>
          <Text style={styles.label}>관리자 번호</Text>
          <Text style={styles.adminNum}>ARHS152DD</Text>
        </View>

        <Text style={[styles.label, { marginTop: 30 }]}>현재 비밀번호</Text>
        <TextInput
          style={styles.input}
          secureTextEntry
          value={currentPassword}
          onChangeText={setCurrentPassword}
        />

        <Text style={styles.label}>변경 비밀번호</Text>
        <TextInput
          style={styles.input}
          secureTextEntry
          value={newPassword}
          onChangeText={setNewPassword}
        />
        <Text style={styles.pwDesc}>
          6~20자 / 영문, 대문자, 소문자, 숫자, 특수문자 중 2가지 이상 조합
        </Text>

        <Text style={styles.label}>새 비밀번호 확인</Text>
        <TextInput
          style={styles.input}
          secureTextEntry
          value={confirmPassword}
          onChangeText={setConfirmPassword}
        />

        <TouchableOpacity style={styles.button} onPress={handleChangePassword}>
          <Text style={styles.buttonText}>비밀번호 변경</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F8F6F6",
    paddingTop: 65,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingBottom: 16,
    justifyContent: "space-between",
    borderBottomWidth: 1,
    borderBottomColor: "#ddd",
  },
  contentBox: {
    marginHorizontal: 28,
  },
  arrowIcon: {
    width: 20,
    height: 20,
    resizeMode: "contain",
    tintColor: "#888",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#222",
    textAlign: "center",
    flex: 1,
  },
  label: {
    fontSize: 16,
    fontWeight: "bold",
    color: "#222",
    marginBottom: 7,
  },
  adminNum: {
    fontSize: 14,
    color: "#BABABA",
    marginTop: 2,
    marginLeft: 2,
    marginBottom: 14,
  },
  input: {
    height: 38,
    backgroundColor: "transparent",
    borderBottomWidth: 1,
    borderBottomColor: "#DADADA",
    fontSize: 15,
    paddingHorizontal: 0,
    marginBottom: 16,
  },
  pwDesc: {
    color: "#B5B5B5",
    fontSize: 11,
    marginTop: -10,
    marginBottom: 13,
    marginLeft: 2,
  },
  button: {
    backgroundColor: "#fff",
    borderRadius: 8,
    marginTop: 32,
    paddingVertical: 14,
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#D5D5D5",
  },
  buttonText: {
    fontSize: 16,
    color: "#818181",
    fontWeight: "bold",
  },
});
